package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.ProductComponent;
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

    @Autowired
    private final SKUGenerator skuGenerator;

    // Define the list of valid SKUs
    private static final List<String> validSkus = Arrays.asList(
            "NOBO3-WHI-LC", "NOBO5-WHI-LC", "1LEDA5-AWA-13", "1LEDA7-AWA-13", "1LEDA9-AWA-13",
            "EWT-OAK-T", "E5AB7-LWH-02", "E5AB9-LWH-02", "E5AS5-LWH-41", "E5AS5-LWH-42",
            "E5AS5-LWH-43", "E5AS7-LWH-41", "E5AS7-LWH-42", "E5AS7-LWH-43", "E5AS9-LWH-41",
            "E5AS9-LWH-42", "E5AS9-LWH-43", "E5BO5-LWH-LC", "E5BO5-LWH-W", "E5BO7-LWH-LC",
            "E5BO7-LWH-W", "E5BO9-LWH-LC", "E5BO9-LWH-W", "E5CL5-LWH-C", "E5CL5-LWH-W",
            "E5CL7-LWH-C", "E5CL7-LWH-W", "E5CL9-LWH-C", "E5CL9-LWH-W", "E5DA5-LWH-13",
            "E5DA5-LWH-22", "E5DA5-LWH-23", "E5DA5-LWH-27", "E5DA5-LWH-32", "E5DA7-LWH-13",
            "E5DA7-LWH-22", "E5DA7-LWH-23", "E5DA7-LWH-27", "E5DA7-LWH-32", "E5DA9-LWH-13",
            "E5DA9-LWH-22", "E5DA9-LWH-23", "E5DA9-LWH-27", "E5DA9-LWH-32", "E5GR5-LWH-W",
            "E5GR7-LWH-W", "E5GR9-LWH-W", "E5NF5-LWH-W", "E5NF7-LWH-W", "E5NF9-LWH-W",
            "E5VA5-LWH-C", "E5VA5-LWH-W", "E5VA7-LWH-C", "E5VA7-LWH-W", "E5VA9-LWH-C",
            "E5VA9-LWH-W", "E5LY5-ESP-LC", "E5LY5-ESP-W", "E5LY7-ESP-LC", "E5LY9-ESP-LC",
            "E5LY9-ESP-W", "E5VA5-ESP-LC", "E5VA5-ESP-W", "E5VA9-ESP-C", "E5VA9-ESP-W",
            "E5AN5-BCH-W", "E5AN7-BCH-W", "E5AN9-BCH-W", "E5AS5-BCH-09", "E5AS5-BCH-12",
            "E5AS7-BCH-09", "E5AS7-BCH-12", "E5AS9-BCH-09", "E5AS9-BCH-12", "E5AV5-BCH-W",
            "E5AV7-BCH-W", "E5AV9-BCH-W", "E5BO5-BCH-W", "E5BO7-BCH-W", "E5BO9-BCH-W",
            "E5CL5-BCH-W", "E5CL7-BCH-W", "E5CL9-BCH-W", "E5DA5-BCH-W", "E5DA7-BCH-W",
            "E5DA9-BCH-W", "E5DO5-BCH-W", "E5DO7-BCH-W", "E5DO9-BCH-W", "E5EN5-BCH-69",
            "E5EN7-BCH-69", "E5EN9-BCH-69", "E5FR5-BCH-02", "E5FR7-BCH-02", "E5FR9-BCH-02",
            "E5GR5-BCH-W", "E5GR7-BCH-W", "E5GR9-BCH-W", "E5KE5-BCH-LC", "E5KE5-BCH-W",
            "E5KE7-BCH-LC", "E5KE7-BCH-W", "E5KE9-BCH-LC", "E5KE9-BCH-W", "E5NF5-BCH-W",
            "E5NF7-BCH-W", "E5NF9-BCH-W", "E5NI5-BCH-W", "E5NI7-BCH-W", "E5NI9-BCH-W",
            "E5PV5-BCH-W", "E5PV7-BCH-W", "E5PV9-BCH-W", "E5QU5-BCH-W", "E5QU7-BCH-W",
            "E5QU9-BCH-W", "E5VA5-BCH-W", "E5VA7-BCH-W", "E5VA9-BCH-W", "E5WE5-BCH-W",
            "E5WE7-BCH-W", "E5WE9-BCH-W", "DAPEL32", "DAPEF29", "DAPEF27", "DAPEF26",
            "DAPEL24", "DAPEF23", "DAPEF22", "DAPEF13", "DAPEF12", "1DLT-WHI-TP",
            "1DLT-SBR-TP", "1DLT-OAK-TP", "1DLT-MAH-TP", "1DLT-ESP-TP", "1DLT-BLK-TP",
            "CLC-ESP-W"
    );


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

    @Transactional
    public void importProductComponents() {
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

            Product product;
            try (CSVReader csvReader = readerBuilder.build()) {
                String productSku;
                String cat;
                String cat2;
                String item1Sku;
                String item2Sku;
                String item3Sku;
                String item4Sku;
                String item5Sku;
                String item6Sku;
                String item7Sku;
                String[] columns;

                while ((columns = csvReader.readNext()) != null) {
                    productSku = getValueByIndex(columns, 0);
                    cat = getValueByIndex(columns, 1);
                    cat2 = getValueByIndex(columns, 2);
                    item1Sku = getValueByIndex(columns, 3);
                    item2Sku = getValueByIndex(columns, 5);
                    item3Sku = getValueByIndex(columns, 7);
                    item4Sku = getValueByIndex(columns, 9);
                    item5Sku = getValueByIndex(columns, 11);
                    item6Sku = getValueByIndex(columns, 13);
                    item7Sku = getValueByIndex(columns, 15);



                    if (productSku.isEmpty()) {
                        continue;
                    }


                    String normalizedSku = productSku.replaceAll("[\\p{C}]", "").trim();


                    if (!validSkus.contains(normalizedSku.trim().toUpperCase())) {
                        continue;
                    }
                    System.out.println("Processing SKU: " + normalizedSku);

                    Optional<Product> optionalProduct = productRepository.findProductBySku(normalizedSku);
                    if (optionalProduct.isPresent()) product = optionalProduct.get();
                    else {
                        product = new Product();
                        product.setSku(normalizedSku.toUpperCase());
                        System.out.println("Inserted new SKU: " + normalizedSku);

                    }
                    product.setCategory(cat);
                    product.setCategory2(cat2);
                    List<ProductComponent> productComponents = product.getComponents();
                    if (productComponents == null) {
                        productComponents = new ArrayList<>();
                        product.setComponents(productComponents); // Ensure it's initialized
                    }
                    if (productComponents.isEmpty()) {
                        // Populate product components
                        addComponentToProduct(productComponents, columns, product, 3, 4);  // item1Sku
                        addComponentToProduct(productComponents, columns, product, 5, 6);  // item2Sku
                        addComponentToProduct(productComponents, columns, product, 7, 8);  // item3Sku
                        addComponentToProduct(productComponents, columns, product, 9, 10); // item4Sku
                        addComponentToProduct(productComponents, columns, product, 11, 12); // item5Sku
                        addComponentToProduct(productComponents, columns, product, 13, 14); // item6Sku
                        addComponentToProduct(productComponents, columns, product, 15, 16);
                    }

                    for (ProductComponent productComponent : productComponents) {
                        System.out.println("Added component SKU: " + productComponent.getComponent().getSku());
                    }
                    System.out.println("Updated SKU: " + normalizedSku + " VALUES : " + productComponents.size());
                    productRepository.save(product);
                }
            }


        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addComponentToProduct(List<ProductComponent> productComponents, String[] columns, Product product, int skuIndex, int quantityIndex) {
        String componentSku = getValueByIndex(columns, skuIndex).toUpperCase();
        if (!componentSku.isEmpty()) {
            Component component = componentRepository.findBySku(componentSku).orElseGet(() -> {
                Component newComponent = new Component();
                newComponent.setSku(componentSku);
                componentRepository.save(newComponent);
                return newComponent;
            });

            ProductComponent productComponent = new ProductComponent();
            productComponent.setProduct(product);
            productComponent.setComponent(component);
            productComponent.setQuantity(Long.parseLong(getValueByIndex(columns, quantityIndex)));
            productComponents.add(productComponent);
        }
    }

}
