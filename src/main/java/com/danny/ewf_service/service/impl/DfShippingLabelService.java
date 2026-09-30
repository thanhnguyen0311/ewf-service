package com.danny.ewf_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DfShippingLabelService {

    private static final String BASE_URL =
            "https://sellingpartnerapi-na.amazon.com/vendor/directFulfillment/shipping/v1/shippingLabels/";

    // Regexes tolerate OCR spacing quirks, e.g. "14LBS", "2 OF2", "25.9,6"
    private static final Pattern DWT     = Pattern.compile("DWT\\s*:?\\s*(\\d+\\s*[,.]\\s*\\d+\\s*[,.]\\s*\\d+)");
    private static final Pattern WEIGHT  = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*LBS?\\b");
    private static final Pattern PKG     = Pattern.compile("\\b(\\d+)\\s*OF\\s*(\\d+)\\b");
    private static final Pattern CITY_ST = Pattern.compile("^(.+?)\\s+([A-Z]{2})\\s+(\\d{5}(?:-\\d{4})?)$");

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String tessDataPath;

    /** Reads the tessdata folder from TESSDATA_PREFIX (default: Ubuntu 24.04 path). */
    public DfShippingLabelService() {
        this(System.getenv("TESSDATA_PREFIX") != null
                ? System.getenv("TESSDATA_PREFIX")
                : LoadLibs.extractTessResources("tessdata").getAbsolutePath());
    }

    /** @param tessDataPath folder containing eng.traineddata */
    public DfShippingLabelService(String tessDataPath) {
        this.tessDataPath = tessDataPath;
    }

    /**
     * OCR a grayscale image through Tess4J's low-level TessAPI.
     * This hands the raw pixels straight to the native Tesseract library and never loads
     * Leptonica/Lept4j, so it works even when the server's Leptonica version doesn't match
     * Lept4j ("undefined symbol: returnErrorFloat1").
     * A new Tesseract handle per call keeps it safe for concurrent requests.
     */
    private String ocr(BufferedImage gray) throws TesseractException {
        TessAPI api = TessAPI.INSTANCE;
        ITessAPI.TessBaseAPI handle = api.TessBaseAPICreate();
        try {
            if (api.TessBaseAPIInit3(handle, tessDataPath, "eng") != 0) {
                throw new TesseractException("Could not load 'eng' from tessdata folder: " + tessDataPath);
            }
            api.TessBaseAPISetPageSegMode(handle, ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK); // psm 6

            // TYPE_BYTE_GRAY = 1 byte per pixel, rows packed with no padding
            byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
            ByteBuffer pixels = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
            pixels.put(data).flip();

            api.TessBaseAPISetImage(handle, pixels, gray.getWidth(), gray.getHeight(), 1, gray.getWidth());

            Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);
            if (textPtr == null) return "";
            try {
                return textPtr.getString(0, "UTF-8");
            } finally {
                api.TessDeleteText(textPtr);
            }
        } finally {
            api.TessBaseAPIEnd(handle);
            api.TessBaseAPIDelete(handle);
        }
    }

    public record LabelDetails(
            String poNumber, String packageId, String trackingNumber, String shipMethodName,
            String customerName, String addressLine, String city, String state, String zip,
            String dwt, String weightLbs, String packageOf, String rawOcrText) {}

    // ---------- 1. PO number -> API -> tracking + label details ----------

    public List<LabelDetails> getLabelDetailsByPo(String poNumber, String accessToken)
            throws IOException, InterruptedException, TesseractException {

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(BASE_URL + URLEncoder.encode(poNumber.trim(), StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(30))
                .header("x-amz-access-token", accessToken)
                .header("Content-Type", "application/json")
                .GET().build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for PO " + poNumber + ": " + response.body());
        }

        JsonNode payload = mapper.readTree(response.body()).path("payload");
        String po = payload.path("purchaseOrderNumber").asText(poNumber);

        List<LabelDetails> result = new ArrayList<>();
        for (JsonNode label : payload.path("labelData")) {
            byte[] png = Base64.getMimeDecoder().decode(label.path("content").asText());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));   // in memory, no file
            if (image == null) {
                throw new IOException("Label for PO " + po + " is not a readable image (format: "
                                      + payload.path("labelFormat").asText() + ")");
            }
            result.add(parseLabel(image, po,
                    label.path("packageIdentifier").asText(),
                    label.path("trackingNumber").asText(),
                    label.path("shipMethodName").asText()));
        }
        return result;
    }

    // ---------- 2. OCR the label image and pull out the fields ----------

    public LabelDetails parseLabel(BufferedImage label, String po, String packageId,
                                   String tracking, String shipMethodName) throws TesseractException {
        // Header block (ship-from, weight, DWT, SHIP TO) is the top ~30% of the label.
        // Cropping skips the barcodes; 2x scaling improves accuracy on small text.
        BufferedImage header = scale(label.getSubimage(0, 0, label.getWidth(),
                (int) (label.getHeight() * 0.30)), 2);

        String text = ocr(header).toUpperCase();

        String dwt = find(DWT, text);
        if (dwt != null) dwt = dwt.replaceAll("\\s", "").replace('.', ',');   // "25,9,6"

        Matcher pkg = PKG.matcher(text);
        String packageOf = pkg.find() ? pkg.group(1) + " of " + pkg.group(2) : null;

        // Lines after "SHIP TO": name, street, then "CITY ST ZIP"
        String name = null, address = null, city = null, state = null, zip = null;
        List<String> lines = text.lines().map(String::trim).filter(l -> !l.isEmpty()).toList();
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).startsWith("SHIP TO")) continue;

            String afterLabel = lines.get(i).replaceFirst("SHIP TO\\s*:?", "").trim();
            List<String> block = new ArrayList<>();
            if (!afterLabel.isEmpty()) block.add(afterLabel);
            block.addAll(lines.subList(i + 1, lines.size()));

            if (!block.isEmpty()) name = block.get(0);
            for (int j = 1; j < block.size(); j++) {
                Matcher m = CITY_ST.matcher(block.get(j));
                if (m.find()) {
                    city = m.group(1); state = m.group(2); zip = m.group(3);
                    address = String.join(", ", block.subList(1, j));
                    break;
                }
            }
            break;
        }

        return new LabelDetails(po, packageId, tracking, shipMethodName,
                name, address, city, state, zip,
                dwt, find(WEIGHT, text), packageOf, text);
    }

    private static String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static BufferedImage scale(BufferedImage src, int factor) {
        BufferedImage out = new BufferedImage(src.getWidth() * factor, src.getHeight() * factor,
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

}