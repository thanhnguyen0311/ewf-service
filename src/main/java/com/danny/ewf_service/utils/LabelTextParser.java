package com.danny.ewf_service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls customer name, address, DWT, weight and package count out of OCR text.
 * Works with both label layouts seen so far:
 *   UPS:    "SHIP TO:" / NAME / STREET / "SAINT PAUL MN 55105-2219"
 *   AMZL:   NAME / STREET / "75061-5459 IRVING , TX United States"
 */
public final class LabelTextParser {

    public record Parsed(String customerName, String addressLine, String city, String state, String zip,
                         String dwt, String weightLbs, String packageOf) {}

    // OCR-tolerant patterns
    private static final Pattern DWT       = Pattern.compile("DWT\\s*:?\\s*(\\d+\\s*[,.]\\s*\\d+\\s*[,.]\\s*\\d+)");
    private static final Pattern WEIGHT    = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*L[BH8][S5]?\\b");   // LBS, LHS, L8S
    private static final Pattern PKG       = Pattern.compile("\\b(\\d+)\\s*OF\\s*(\\d+)\\b");
    // "SAINT PAUL MN 55105-2219"
    private static final Pattern CITY_FIRST = Pattern.compile("^(.+?)\\s*,?\\s+([A-Z]{2})\\s+(\\d{5}(?:-\\d{4})?)\\b");
    // "75061-5459 IRVING , TX UNITED STATES"
    private static final Pattern ZIP_FIRST  = Pattern.compile("^(\\d{5}(?:-\\d{4})?)\\s+(.+?)\\s*,\\s*([A-Z]{2})\\b");
    // Street-type lines: start with a number, PO box, apt, suite ...
    private static final Pattern STREET     = Pattern.compile("^(\\d|P\\.?\\s?O\\.?\\s+BOX|APT|UNIT|STE\\b|SUITE|#|BLDG|FL\\b|FLOOR|RM\\b|ROOM)");

    private LabelTextParser() {}

    public static Parsed parse(String ocrText) {
        String text = ocrText.toUpperCase();
        List<String> lines = text.lines().map(String::trim).filter(l -> !l.isEmpty()).toList();

        String dwt = find(DWT, text);
        if (dwt != null) dwt = dwt.replaceAll("\\s", "").replace('.', ',');

        Matcher pkg = PKG.matcher(text);
        String packageOf = pkg.find() ? pkg.group(1) + " of " + pkg.group(2) : null;

        // ---- find the customer's city/state/zip line ----
        int shipTo = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("SHIP TO")) { shipTo = i; break; }
        }

        int zipLine = -1;
        String city = null, state = null, zip = null;
        for (int i = Math.max(shipTo, 0); i < lines.size(); i++) {
            String l = lines.get(i);
            Matcher a = ZIP_FIRST.matcher(l);
            Matcher b = CITY_FIRST.matcher(l);
            boolean hit = false;
            if (a.find())      { zip = a.group(1); city = a.group(2).trim(); state = a.group(3); hit = true; }
            else if (b.find()) { city = b.group(1).trim(); state = b.group(2); zip = b.group(3); hit = true; }
            if (hit) {
                zipLine = i;
                if (shipTo >= 0) break;   // UPS: first match after SHIP TO is the customer
                // no SHIP TO: keep going, the LAST match is the customer (skips a ship-from address)
            }
        }

        // ---- walk upward: street lines, then the name ----
        String name = null, address = null;
        if (zipLine > 0) {
            int i = zipLine - 1;
            List<String> street = new ArrayList<>();
            while (i >= 0 && i > shipTo && STREET.matcher(lines.get(i)).find()) {
                street.add(0, lines.get(i));
                i--;
            }
            address = street.isEmpty() ? null : String.join(", ", street);

            if (i >= 0) {
                String candidate = lines.get(i).replaceFirst("^SHIP TO\\s*:?", "").trim();
                if (!candidate.isEmpty()) name = candidate;
            }
        }

        return new Parsed(name, address, city, state, zip, dwt, find(WEIGHT, text), packageOf);
    }

    private static String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }
}