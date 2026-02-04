package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
@AllArgsConstructor
public class ProductsImport {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final ComponentRepository componentRepository;

    private final SKUGenerator skuGenerator;

    @Transactional
    public void importProductDetails() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))) {


            CSVParserBuilder parserBuilder = new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withEscapeChar('\\')
                    .withStrictQuotes(false)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreQuotations(false)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);

            CSVParser parser = parserBuilder.build();

            // Create reader with the configured parser
            CSVReaderBuilder readerBuilder = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .withMultilineLimit(-1); // No limit on multiline fields

            Map<String, Product> productMap = new HashMap<>();
//            List<Product> products = productRepository.findAllProducts();
//            System.out.println("Total Products: " + products.size());
//            for (Product product : products) {
//                String normalizedSku = product.getSku().replaceAll("[\\p{C}]", "").trim();
//                productMap.put(normalizedSku, product);
//            }
            Product product;
            try (CSVReader csvReader = readerBuilder.build()) {
                String productSku;
                String title;
                String shippingMethod;
                String asin;
                String description;
                String html;
                String mainCategory;
                String subCategory;
                String chairType;
                String finish;
                String sizeShape;
                String style;
                String pieces;
                String collection;
                String[] columns;

                while ((columns = csvReader.readNext()) != null) {
                    productSku = getValueByIndex(columns, 0);
                    title = getValueByIndex(columns, 1);
                    shippingMethod = getValueByIndex(columns, 2);
                    asin = getValueByIndex(columns, 3);
                    description = getValueByIndex(columns, 4);
                    html = getValueByIndex(columns, 5);
                    mainCategory = getValueByIndex(columns, 6);
                    subCategory = getValueByIndex(columns, 7);
                    chairType = getValueByIndex(columns, 8);
                    finish = getValueByIndex(columns, 9);
                    sizeShape = getValueByIndex(columns, 10);
                    style = getValueByIndex(columns, 11);
                    pieces = getValueByIndex(columns, 12);
                    collection = getValueByIndex(columns, 13);


                    if (productSku.isEmpty()) {
                        continue;
                    }
                    String normalizedSku = productSku.replaceAll("[\\p{C}]", "").trim();

                    Optional<Product> optionalProduct = productRepository.findProductBySku(normalizedSku);
                    if (optionalProduct.isPresent()) product = optionalProduct.get();
                    else {
                        product = new Product();
                        product.setSku(normalizedSku.toUpperCase());
                        System.out.println("Inserted new SKU: " + normalizedSku);

                    }
                    if (product.getLocalSku() == null) product.setLocalSku(skuGenerator.generateNewSKU(normalizedSku.toUpperCase()));

                    if (!title.isEmpty()) product.setTitle(title);
                    if (!shippingMethod.isEmpty()) product.setShippingMethod(shippingMethod);
                    if (!asin.isEmpty()) product.setAsin(asin);

                    ProductDetail productDetail = product.getProductDetail();
                    if (productDetail == null) productDetail = new ProductDetail();

                    if(!description.isEmpty()) productDetail.setDescription(description);
                    if (!html.isEmpty()) productDetail.setHtmlDescription(html);
                    if (!mainCategory.isEmpty()) productDetail.setMainCategory(mainCategory);
                    if (!subCategory.isEmpty()) productDetail.setSubCategory(subCategory);
                    if (!chairType.isEmpty()) productDetail.setChairType(chairType);
                    if (!finish.isEmpty()) productDetail.setFinish(finish);
                    if (!sizeShape.isEmpty()) productDetail.setSizeShape(sizeShape);
                    if (!style.isEmpty()) productDetail.setStyle(style);
                    if (!pieces.isEmpty()) productDetail.setPieces(pieces);
                    if (!collection.isEmpty()) productDetail.setCollection(collection);

                    product.setProductDetail(productDetail);

                    productRepository.save(product);
                    System.out.println("Updated SKU: " + normalizedSku);
                }
            }


        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getValueByIndex(String[] array, int index) {
        String value = "";
        if (index < array.length && array[index] != null) { // Check for null
            value = array[index].trim();
        }

//        System.out.println("Column " + index + " value: " + value);
        return value;
    }


    public void importProductWholesales() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
            int notFound = 0;
            String line;
            String productSku;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                productSku = columns[0].trim().toUpperCase();

                if (productSku.isEmpty()) {
                    continue;
                }

                Optional<Product> optionalProduct = productRepository.findBySku(productSku.toUpperCase());
                Product product;

                if (optionalProduct.isPresent()) {
                    product = optionalProduct.get();
                    ProductWholesales productWholesales = product.getWholesales();
                    if (productWholesales == null) {
                        productWholesales = new ProductWholesales();
                    }
                    productWholesales.setEwfmain(true);
                    product.setWholesales(productWholesales);
                    productRepository.save(product);
                } else {
                    System.out.println("Product not found: " + productSku);
                    notFound++;
                }
                System.out.println("Saved product " + productSku);
            }

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public void importDimensions() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length < 2) {
                    continue;
                }

                String sku = columns[0].trim();
                String lwh = columns[1].trim();

                if (sku.isEmpty()) {
                    continue;
                }
                try {
                    Product product;
                    Optional<Product> optionalProduct = productRepository.findBySku(sku);
                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                    } else {
                        System.err.println("Error processing row for component " + sku);
                        continue;
                    }
                    Dimension dimension = product.getDimension();
                    if (dimension == null) {
                        dimension = new Dimension();
                    }
                    dimension.setLwh(lwh);
                    product.setDimension(dimension);
                    productRepository.save(product);
                    System.out.println("Successfully Updated Component SKU : " + sku + " VALUES : " + dimension);


                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + sku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    public void importProductPrice() {
        try (InputStream file = getClass().getResourceAsStream("/data/beds.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length < 1) {
                    continue;
                }

                String sku = columns[0].trim().toUpperCase();
//                double qb1 = Double.parseDouble(columns[1].trim());
//                double qb2 = Double.parseDouble(columns[2].trim());
//                double qb3 = Double.parseDouble(columns[3].trim());
//                double qb4 = Double.parseDouble(columns[4].trim());
//                double qb5 = Double.parseDouble(columns[5].trim());
//                double qb6 = Double.parseDouble(columns[6].trim());


                if (sku.isEmpty()) {
                    continue;
                }

                try {
                    Product product;
                    Optional<Product> optionalProduct = productRepository.findBySku(sku);
                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                        Price price = product.getPrice();
                        if (price == null) price = new Price();
//                        price.setQB1(qb1);
//                        price.setQB2(qb2);
//                        price.setQB3(qb3);
//                        price.setQB4(qb4);
//                        price.setQB5(qb5);
//                        price.setQB6(qb6);
                        price.setShippingCost(200.0);
                        product.setPrice(price);
                        productRepository.save(product);
                        System.out.println("Successfully Updated product : " + sku);
                    }

                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + sku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    public void importProductShipping() {
        try (InputStream file = getClass().getResourceAsStream("/data/shipping.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length < 2) {
                    continue;
                }

                String sku = columns[0].trim().toUpperCase();
                String method = columns[1].trim();

                if (sku.isEmpty()) {
                    continue;
                }

                try {
                    Product product;
                    Optional<Product> optionalProduct = productRepository.findBySku(sku);
                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                        product.setShippingMethod(method);
                        productRepository.save(product);
                        System.out.println("Successfully Updated product : " + sku + " VALUES : " + method);
                    }

                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + sku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

}
