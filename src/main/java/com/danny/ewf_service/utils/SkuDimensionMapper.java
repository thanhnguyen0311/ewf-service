package com.danny.ewf_service.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the published dimension sheet (CSV) and builds a mapping list:
 *   Group SKU -> Component SKU (+ box qty, length, width, height, weight)
 *
 * Sheet layout (repeats for Item 1..N):
 *   SKUs | Total Box | Item N | Box | Item N Length | Item N Width | Item N Height | Item N Weight | ...
 *
 * Columns are located by header name, so adding "Item 5", "Item 6", ... later works automatically.
 *
 * Requires Java 17+ (uses records and java.net.http.HttpClient).
 * Run:  java SkuDimensionMapper.java            (reads from the URL)
 *       java SkuDimensionMapper.java local.csv  (reads from a local file, for testing)
 */
public class SkuDimensionMapper {

    private static final String SHEET_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vRplJNeJwIR7_p65K72gXeRJsIyPaDAV3fN5szR5gjJogkH82N3RlYB9q5lMFApxKQ2OOSD2MEZw78I/pub?gid=1338306882&single=true&output=csv";

    /** One row of the mapping list. */
    public record SkuMapping(
            String groupSku,
            int totalBox,
            String componentSku,
            int boxQty,
            double length,
            double width,
            double height,
            double weight) {}

    /** Column positions for one "Item N" block. */
    private record ItemColumns(int itemNo, int sku, int box, int length, int width, int height, int weight) {}

    private static final Pattern ITEM_HEADER = Pattern.compile("(?i)^item\\s*(\\d+)$");

    // ------------------------------------------------------------------ public API

    /** Downloads the sheet from SHEET_URL and returns the full SKU mapping list. */
    public static List<SkuMapping> getSkuMappings() throws IOException, InterruptedException {
        return getSkuMappings(SHEET_URL);
    }

    /** Downloads the sheet from the given CSV url and returns the full SKU mapping list. */
    public static List<SkuMapping> getSkuMappings(String csvUrl) throws IOException, InterruptedException {
        return buildMappings(parseCsv(new StringReader(downloadCsv(csvUrl))));
    }

    // ------------------------------------------------------------------ cached mappings

    private static volatile List<SkuMapping> cachedMappings;

    /** Returns the mappings, downloading the sheet only the first time it is called. */
    public static List<SkuMapping> getCachedMappings() throws IOException, InterruptedException {
        if (cachedMappings == null) {
            synchronized (SkuDimensionMapper.class) {
                if (cachedMappings == null) {
                    cachedMappings = getSkuMappings();
                }
            }
        }
        return cachedMappings;
    }

    /** Re-downloads the sheet (call this if the sheet was updated). */
    public static synchronized void reloadMappings() throws IOException, InterruptedException {
        cachedMappings = getSkuMappings();
    }

    /** Nearest component SKU, using the cached mappings. */
    public static String findComponentSku(String groupSku, String dimensions)
            throws IOException, InterruptedException {
        return findComponentSku(getCachedMappings(), groupSku, dimensions);
    }

    /** Nearest component SKU within maxDiff, using the cached mappings. */
    public static String findComponentSku(String groupSku, String dimensions, double maxDiff)
            throws IOException, InterruptedException {
        return findComponentSku(getCachedMappings(), groupSku, dimensions, maxDiff);
    }

    /** Returns the components (with dimensions) of one group SKU, or an empty list if not found. */
    public static List<SkuMapping> getComponents(List<SkuMapping> mappings, String groupSku) {
        return mappings.stream()
                .filter(m -> m.groupSku().equalsIgnoreCase(groupSku.trim()))
                .toList();
    }


    public static String findComponentSku(List<SkuMapping> mappings, String groupSku, String dimensions) {
        return findComponentSku(mappings, groupSku, dimensions, Double.MAX_VALUE);
    }

    public static String findComponentSku(List<SkuMapping> mappings, String groupSku,
                                          String dimensions, double maxDiff) {
        String[] parts = dimensions.trim().split("[,xX\\s]+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Dimension must be \"Length,Width,Height\", got: " + dimensions);
        }
        double l = Double.parseDouble(parts[0]);
        double w = Double.parseDouble(parts[1]);
        double h = Double.parseDouble(parts[2]);

        SkuMapping best = null;
        double bestDiff = Double.MAX_VALUE;
        for (SkuMapping m : getComponents(mappings, groupSku)) {
            double diff = Math.abs(m.length() - l) + Math.abs(m.width() - w) + Math.abs(m.height() - h);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = m;
            }
        }
        return (best != null && bestDiff <= maxDiff) ? best.componentSku() : null;
    }

    // ------------------------------------------------------------------ download

    public static String downloadCsv(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)   // Google published links redirect
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download sheet, HTTP " + response.statusCode());
        }
        return response.body();
    }

    // ------------------------------------------------------------------ mapping

    public static List<SkuMapping> buildMappings(List<List<String>> rows) {
        List<SkuMapping> result = new ArrayList<>();
        if (rows.isEmpty()) return result;

        List<String> header = rows.get(0);
        int skuCol = findColumn(header, "SKUs", "SKU");
        int totalBoxCol = findColumn(header, "Total Box");
        if (skuCol < 0) throw new IllegalStateException("'SKUs' column not found in header: " + header);

        List<ItemColumns> items = findItemColumns(header);

        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            String groupSku = cell(row, skuCol);
            if (groupSku.isEmpty()) continue;
            int totalBox = (int) toNumber(cell(row, totalBoxCol));

            for (ItemColumns ic : items) {
                String component = cell(row, ic.sku());
                if (component.isEmpty()) continue;   // this group has no Item N

                int qty = (int) toNumber(cell(row, ic.box()));
                result.add(new SkuMapping(
                        groupSku,
                        totalBox,
                        component,
                        qty == 0 ? 1 : qty,
                        toNumber(cell(row, ic.length())),
                        toNumber(cell(row, ic.width())),
                        toNumber(cell(row, ic.height())),
                        toNumber(cell(row, ic.weight()))));
            }
        }
        return result;
    }

    public static Map<String, List<SkuMapping>> groupByGroupSku(List<SkuMapping> mappings) {
        Map<String, List<SkuMapping>> map = new LinkedHashMap<>();
        for (SkuMapping m : mappings) {
            map.computeIfAbsent(m.groupSku(), k -> new ArrayList<>()).add(m);
        }
        return map;
    }

    /** Finds every "Item N" column and the Box / Length / Width / Height / Weight columns that belong to it. */
    private static List<ItemColumns> findItemColumns(List<String> header) {
        List<ItemColumns> items = new ArrayList<>();
        for (int c = 0; c < header.size(); c++) {
            Matcher m = ITEM_HEADER.matcher(header.get(c).trim());
            if (!m.matches()) continue;
            int n = Integer.parseInt(m.group(1));
            String prefix = "Item " + n + " ";

            // "Box" headers are all named the same, so take the one right after "Item N"
            int box = (c + 1 < header.size() && header.get(c + 1).trim().equalsIgnoreCase("Box")) ? c + 1 : -1;

            items.add(new ItemColumns(n, c, box,
                    findColumn(header, prefix + "Length"),
                    findColumn(header, prefix + "Width"),
                    findColumn(header, prefix + "Height"),
                    findColumn(header, prefix + "Weight")));
        }
        return items;
    }

    private static int findColumn(List<String> header, String... names) {
        for (String name : names) {
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).trim().replaceAll("\\s+", " ").equalsIgnoreCase(name)) return i;
            }
        }
        return -1;
    }

    private static String cell(List<String> row, int col) {
        return (col >= 0 && col < row.size()) ? row.get(col).trim() : "";
    }

    private static double toNumber(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    // ------------------------------------------------------------------ CSV

    /** Minimal RFC-4180 CSV parser (handles quotes, escaped quotes, commas and newlines inside quotes). */
    public static List<List<String>> parseCsv(Reader in) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        BufferedReader br = new BufferedReader(in);
        int ch;
        while ((ch = br.read()) != -1) {
            char c = (char) ch;
            if (inQuotes) {
                if (c == '"') {
                    br.mark(1);
                    int next = br.read();
                    if (next == '"') field.append('"');
                    else { inQuotes = false; if (next != -1) br.reset(); }
                } else field.append(c);
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                row.add(field.toString()); field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r') { br.mark(1); if (br.read() != '\n') br.reset(); }
                row.add(field.toString()); field.setLength(0);
                rows.add(row); row = new ArrayList<>();
            } else field.append(c);
        }
        if (field.length() > 0 || !row.isEmpty()) { row.add(field.toString()); rows.add(row); }
        return rows;
    }
}