package com.danny.ewf_service.service.impl;


import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.service.SpreadsheetService;
import com.danny.ewf_service.utils.imports.SKUGenerator;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class SpreadsheetServiceImpl implements SpreadsheetService {

    private final String APPLICATION_NAME = "ewf-app";
    private final String PRODUCT_SHEET_ID = "1eKLa6EWdJ4DWTLSNA3zp7ahQzJoVdxMxcjfU5C68z0A";

    private final String SHEET_TAB = "All Product";
    private final String HEADER_RANGE = "'" + SHEET_TAB + "'!A2:BL2";

    private final String CREDENTIALS_PATH = "src/main/resources/service-account.json";


    @Autowired
    private SKUGenerator skuGenerator;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;


    @Override
    public Sheets getSheetsService() throws Exception {
        InputStream credentialsStream = new FileInputStream(CREDENTIALS_PATH);

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Converts 0-based column index to spreadsheet letter (0 -> A, 1 -> B, ... 26 -> AA)
    @Override
    public String columnIndexToLetter(int index) {
        StringBuilder sb = new StringBuilder();
        while (index >= 0) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return sb.toString();
    }

    @Override
    public void updateProductData(String[] targetHeaders) throws Exception{
        Sheets sheetsService = getSheetsService();

        Product product;
        ProductDetail productDetail;

        // 1. Read header row
        ValueRange headerResponse = sheetsService.spreadsheets().values()
                .get(PRODUCT_SHEET_ID, HEADER_RANGE)
                .execute();

        List<List<Object>> headerValues = headerResponse.getValues();

        if (headerValues == null || headerValues.isEmpty()) {
            System.out.println("No header data found in tab: " + SHEET_TAB);
            return;
        }

        List<Object> headerRow = headerValues.get(0);

        System.out.println("Headers found in '" + SHEET_TAB + "' (" + headerRow.size() + " columns):");
        System.out.println("----------------------------------------------------");

        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int col = 0; col < headerRow.size(); col++) {
            String columnLetter = columnIndexToLetter(col);
            Object header = headerRow.get(col);
            System.out.println(columnLetter + " -> " + (header != null ? header : "(empty)"));
            if (header != null && !header.toString().trim().isEmpty()) {
                headerIndexMap.put(header.toString().trim(), col);
            }
        }

        // 2. Locate SKU column
        Integer skuColIndex = headerIndexMap.get("SKU");
        if (skuColIndex == null) {
            System.out.println("SKU column not found in headers.");
            return;
        }

        // 3. Validate requested target headers exist
        List<String> validHeaders = new ArrayList<>();
        for (String h : targetHeaders) {
            if (headerIndexMap.containsKey(h)) {
                validHeaders.add(h);
            } else {
                System.out.println("Warning: header not found in sheet: " + h);
            }
        }
        if (validHeaders.isEmpty()) {
            System.out.println("No valid target headers to read.");
            return;
        }

        // 4. Read data rows (adjust range to match where your data actually starts)
        ValueRange dataResponse = sheetsService.spreadsheets().values()
                .get(PRODUCT_SHEET_ID, SHEET_TAB + "!A3:Z")
                .execute();

        List<List<Object>> rows = dataResponse.getValues();
        if (rows == null || rows.isEmpty()) {
            System.out.println("No data rows found in tab: " + SHEET_TAB);
            return;
        }

        System.out.println("----------------------------------------------------");
        System.out.println("Row values for SKU + " + validHeaders);
        System.out.println("----------------------------------------------------");

        for (List<Object> row : rows) {
            if (skuColIndex >= row.size()) {
                continue;
            }
            Object skuCell = row.get(skuColIndex);
            if (skuCell == null || skuCell.toString().trim().isEmpty()) {
                continue;
            }
            String sku = skuCell.toString().trim();

            StringBuilder sb = new StringBuilder();
            sb.append("SKU=").append(sku);
            Optional<Product> optionalProduct = productRepository.findProductBySku(sku);
            if (optionalProduct.isPresent()) product = optionalProduct.get();
            else {
                product = new Product();
                product.setLocalSku(skuGenerator.generateNewSKU(sku));
                product.setSku(sku);
                System.out.println("New product: " + product.getLocalSku());
            }
            productDetail = product.getProductDetail();
            if (productDetail == null) productDetail = new ProductDetail();
            for (String header : validHeaders) {
                int colIdx = headerIndexMap.get(header);
                String value = (colIdx < row.size() && row.get(colIdx) != null)
                        ? row.get(colIdx).toString().trim()
                        : "";
                if (!value.isEmpty()) {
                    if (header.equals("Type")) product.setType(value);
                    if (header.equals("Category")) product.setCategory(value);
                    if (header.equals("Shipping")) product.setShippingMethod(value);
                    if (header.equals("Main Category")) productDetail.setMainCategory(value);
                    if (header.equals("Sub Category")) productDetail.setSubCategory(value);
                    if (header.equals("Luxe")) productDetail.setIsLuxe(value.equals("Luxe"));
                    if (header.equals("Group ID")) productDetail.setGroupID(value);
                    if (header.equals("UPC")) product.setUpc(value);
                    if (header.equals("Finish")) productDetail.setFinish(value);
                    if (header.equals("PIECES")) productDetail.setPieces(value);
                    if (header.equals("Chair Type")) productDetail.setChairType(value);
                    if (header.equals("Bed Types")) productDetail.setBedType(value);
                    if (header.equals("Size & Shape")) productDetail.setSizeShape(value);
                    if (header.equals("Style")) productDetail.setStyle(value);
                    if (header.equals("Collection")) productDetail.setCollection(value);
                    if (header.equals("ASIN")) product.setAsin(value);

                    if (header.equals("Title")) product.setTitle(value);
                    if (header.equals("Description")) productDetail.setDescription(value);
                    if (header.equals("HTML Description")) productDetail.setHtmlDescription(value);
                }

                sb.append(", ").append(header).append("=").append(value);
            }
            ProductWholesales productWholesales = product.getWholesales();
            if (productWholesales == null) productWholesales = new ProductWholesales();
            if (product.getCategory().equals("1ADis")) {
                productWholesales.setEwfdirect(false);
                product.setDiscontinued(true);
            }

            product.setWholesales(productWholesales);
            product.setProductDetail(productDetail);
            productRepository.save(product);
            System.out.println("Updated product: " + product.getSku());

            System.out.println(sb);
        }
    }
}
