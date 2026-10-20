package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.repository.OrderRepository;
import com.danny.ewf_service.service.OrderService;
import com.danny.ewf_service.service.amz.TokenService;
import com.danny.ewf_service.utils.LabelTextParser;
import com.danny.ewf_service.utils.SkuDimensionMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;

/**
 * PO number -> Amazon DF getShippingLabel -> tracking number + customer name / address / DWT
 * read from the PNG label with Tess4J OCR (in memory, no files saved).
 */

@Service
public class DfShippingLabelService {

    private static final String ORDERS_URL =
            "https://sellingpartnerapi-na.amazon.com/vendor/directFulfillment/orders/2021-12-28/purchaseOrders/";

    private static final String BASE_URL =
            "https://sellingpartnerapi-na.amazon.com/vendor/directFulfillment/shipping/v1/shippingLabels/";
    private static final Font SKU_FONT = loadSkuFont();

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String tessDataPath;
    /** Result of processing one PO: edited images + group SKU + quantity + component SKU per package (in label order). */
    public record PoLabels(String groupSku, Long quantity, List<String> skus, List<BufferedImage> images) {}

    /** What the mapping holds for one PO. */
    public record PoMapping(String groupSku, Long quantity, List<String> skus) {}

    /** PDF bytes plus { PO : { groupSku, quantity, skus } } */
    public record LabelPdfResult(byte[] pdf, Map<String, PoMapping> mapping) {}

    /** Values read from the DF order: items[0].vendorProductIdentifier and items[0].orderedQuantity.amount */
    public record OrderInfo(String groupSku, Long quantity) {}


    private static Font loadSkuFont() {
        try (var in = DfShippingLabelService.class.getResourceAsStream("src/main/resources/JetBrainsMono-Bold.ttf")) {
            if (in == null) throw new IOException("font not found");
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.BOLD, 32); // fallback
        }
    }

    /** Uses TESSDATA_PREFIX if set, otherwise the eng.traineddata bundled in resources/tessdata. */
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
            String dwt, String weightLbs, String packageOf, String rawOcrText, String groupSku, Long quantity,
            @JsonIgnore BufferedImage label) {
        public LabelDetails withPackageOf(String v) {
            return new LabelDetails(poNumber, packageId, trackingNumber, shipMethodName, customerName,
                    addressLine, city, state, zip, dwt, weightLbs, v, rawOcrText, groupSku, quantity, label);
        }

        public LabelDetails withGroupSku(String v) {
            return new LabelDetails(poNumber, packageId, trackingNumber, shipMethodName, customerName,
                    addressLine, city, state, zip, dwt, weightLbs, packageOf, rawOcrText, v, quantity, label);
        }
        public LabelDetails withQuantity(Long v) {
            return new LabelDetails(poNumber, packageId, trackingNumber, shipMethodName, customerName,
                    addressLine, city, state, zip, dwt, weightLbs, packageOf, rawOcrText, groupSku, v, label);
        }

    }

    // ---------- 1. PO number -> API -> tracking + label details ----------

    public List<LabelDetails> getLabelDetailsByPo(String poNumber, String accessToken)
            throws IOException, InterruptedException, TesseractException {

        String url = BASE_URL + URLEncoder.encode(poNumber.trim(), StandardCharsets.UTF_8);
        JsonNode payload = getJson(url, accessToken, poNumber).path("payload");
        String po = payload.path("purchaseOrderNumber").asText(poNumber);

        OrderInfo orderInfo = getOrderInfoByPo(po, accessToken);   // once per PO

        JsonNode labels = payload.path("labelData");
        List<LabelDetails> result = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            JsonNode label = labels.get(i);
            byte[] png = Base64.getMimeDecoder().decode(label.path("content").asText());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) {
                throw new IOException("Label for PO " + po + " is not a readable image (format: "
                                      + payload.path("labelFormat").asText() + ")");
            }
            LabelDetails d = parseLabel(image, po,
                    label.path("packageIdentifier").asText(),
                    label.path("trackingNumber").asText(),
                    label.path("shipMethodName").asText());

            if (d.packageOf() == null) {
                d = d.withPackageOf((i + 1) + " of " + labels.size());
            }
            result.add(d.withGroupSku(orderInfo.groupSku())
                    .withQuantity(orderInfo.quantity()));
        }
        return result;
    }

    // ---------- 2. OCR the label image and pull out the fields ----------

    public LabelDetails parseLabel(BufferedImage label, String po, String packageId,
                                   String tracking, String shipMethodName) throws TesseractException {
        BufferedImage header = scale(label.getSubimage(0, 0, label.getWidth(),
                (int) (label.getHeight() * 0.30)), 2);

        String text = ocr(header);
        LabelTextParser.Parsed p = LabelTextParser.parse(text);

        return new LabelDetails(po, packageId, tracking, shipMethodName,
                p.customerName(), p.addressLine(), p.city(), p.state(), p.zip(),
                p.dwt(), p.weightLbs(), p.packageOf(), text,
                null, null,  // groupSku and quantity are filled in by getLabelDetailsByPo
                label);
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

    // ---------- 3. PO -> labels with SKU text -> one PDF (in memory) ----------

    public byte[] getEditedLabelPdfByPos(List<String> poNumbers)
            throws IOException, InterruptedException, TesseractException {
        return getEditedLabelPdfWithMappingByPos(poNumbers).pdf();
    }

    private PoLabels buildEditedLabels(String poNumber, String accessToken)
            throws IOException, InterruptedException, TesseractException {
        List<BufferedImage> edited = new ArrayList<>();
        List<String> skus = new ArrayList<>();
        String groupSku = null;
        Long quantity = null;

        for (LabelDetails d : getLabelDetailsByPo(poNumber, accessToken)) {
            groupSku = d.groupSku();   // same for every label of the PO
            quantity = d.quantity();   // same for every label of the PO
            String sku = (d.groupSku() == null || d.dwt() == null)
                    ? null
                    : SkuDimensionMapper.findComponentSku(d.groupSku(), d.dwt());
            skus.add(sku); // keeps label order; null = not found by DWT
            edited.add(addSkuText(d.label(), d.groupSku(), sku, poNumber));
        }
        return new PoLabels(groupSku, quantity, skus, edited);
    }
    public LabelPdfResult getEditedLabelPdfWithMappingByPos(List<String> poNumbers)
            throws IOException, InterruptedException, TesseractException {
        String accessToken = TokenService.getAccessToken();

        List<String> pos = poNumbers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(po -> !po.isEmpty())
                .distinct()
                .toList();

        List<BufferedImage> edited = new ArrayList<>();
        Map<String, PoMapping> mapping = new LinkedHashMap<>(); // keeps PO order

        for (String po : pos) {
            PoLabels r = buildEditedLabels(po, accessToken);
            edited.addAll(r.images());

            // groupSku is now a value, not a map key, so null is fine for Jackson
            String groupSku = r.groupSku() != null ? r.groupSku() : "UNKNOWN";
            mapping.put(po, new PoMapping(groupSku, r.quantity(), r.skus()));
        }

        if (edited.isEmpty()) {
            throw new IOException("No labels found for POs " + pos);
        }
        return new LabelPdfResult(toPdf(edited), mapping);
    }

    /** Fetches every label for the PO, adds the SKU lines, returns a PDF (one label per page). */
    public byte[] getEditedLabelPdfByPo(String poNumber)
            throws IOException, InterruptedException, TesseractException {
        return getEditedLabelPdfByPos(List.of(poNumber));
    }

    /** Calls getShippingLabel and decodes each base64 label into an image. Nothing is written to disk. */
    private List<BufferedImage> fetchLabelImages(String poNumber, String accessToken)
            throws IOException, InterruptedException {
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
        List<BufferedImage> images = new ArrayList<>();
        for (JsonNode label : payload.path("labelData")) {
            byte[] png = Base64.getMimeDecoder().decode(label.path("content").asText());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) {
                throw new IOException("Label for PO " + poNumber + " is not a readable image (format: "
                                      + payload.path("labelFormat").asText() + ")");
            }
            images.add(image);
        }
        return images;
    }

    /** Draws "Group SKU" and "SKU" below "BILLING: P/P". Returns a new image; the original is untouched. */
    public BufferedImage addSkuText(BufferedImage label, String groupSku, String sku, String poNumber) {
        // Grayscale copy: works for 1-bit labels and keeps the PDF small
        BufferedImage out = new BufferedImage(label.getWidth(), label.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.drawImage(label, 0, 0, null);

        // Positions were measured on an 800px-wide label; scale for other sizes
        double s = label.getWidth() / 800.0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF); // crisp for thermal printers
        g.setColor(Color.BLACK);

        int x = (int) (270 * s);

// Group line: smaller font
        g.setFont(SKU_FONT.deriveFont((float) (24 * s)));
        g.drawString("Group: " + groupSku, x, (int) (1070 * s));

// SKU line: original size
        if (sku == null) {
            g.dispose();
            return out;
        }
        g.setFont(SKU_FONT.deriveFont((float) (32 * s)));
        g.drawString("SKU : " + sku, x, (int) (1110 * s));

        g.dispose();
        return out;
    }

    /** Puts each image on its own 4x6 inch page and returns the PDF bytes. */
    public static byte[] toPdf(List<BufferedImage> labels) throws IOException {
        PDRectangle pageSize = new PDRectangle(4 * 72, 6 * 72); // 4x6 in, in points

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            for (BufferedImage label : labels) {
                PDPage page = new PDPage(pageSize);
                doc.addPage(page);

                // Lossless (not JPEG) so barcodes stay scannable
                PDImageXObject img = LosslessFactory.createFromImage(doc, label);

                // Fit inside the page, keep aspect ratio, align to top
                float scale = Math.min(pageSize.getWidth() / img.getWidth(),
                        pageSize.getHeight() / img.getHeight());
                float w = img.getWidth() * scale;
                float h = img.getHeight() * scale;
                float x = (pageSize.getWidth() - w) / 2;
                float y = pageSize.getHeight() - h;

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.drawImage(img, x, y, w, h);
                }
            }
            doc.save(bos);
            return bos.toByteArray();
        }
    }

    private JsonNode getJson(String url, String accessToken, String poNumber)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("x-amz-access-token", accessToken)
                .header("Content-Type", "application/json")
                .GET().build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for PO " + poNumber
                                  + " (" + url + "): " + response.body());
        }
        return mapper.readTree(response.body());
    }

    /** One call to the orders API -> group SKU + ordered quantity (either may be null if missing). */
    public OrderInfo getOrderInfoByPo(String poNumber, String accessToken)
            throws IOException, InterruptedException {
        String url = ORDERS_URL + URLEncoder.encode(poNumber.trim(), StandardCharsets.UTF_8);
        JsonNode root = getJson(url, accessToken, poNumber);
        // 2021-12-28 returns the order at the top level; older versions wrap it in "payload"
        JsonNode order = root.has("payload") ? root.path("payload") : root;

        JsonNode items = order.path("orderDetails").path("items");
        if (!items.isArray() || items.isEmpty()) return new OrderInfo(null, null);

        JsonNode item = items.get(0);

        String sku = item.path("vendorProductIdentifier").asText("");
        String groupSku = sku.isBlank() ? null : sku;

        JsonNode amount = item.path("orderedQuantity").path("amount");
        Long quantity = (amount.isMissingNode() || amount.isNull() || !amount.canConvertToLong()
                                                                      && !amount.isTextual())
                ? null
                : amount.asLong();

        return new OrderInfo(groupSku, quantity);
    }

    /** orderDetails.items[0].vendorProductIdentifier, or null if missing. */
    public String getGroupSkuByPo(String poNumber, String accessToken)
            throws IOException, InterruptedException {
        return getOrderInfoByPo(poNumber, accessToken).groupSku();
    }
}