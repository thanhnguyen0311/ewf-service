package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.product.ProductDetail;
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
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

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

            try (CSVReader csvReader = readerBuilder.build()) {
                String productSku;
                String upc;
                String asin;
                String name;
                String title;
                String description;
                String htmlDescription;
                String finish;
                String sizeShape;
                String collection;
                String lwh;
                String style;
                String pieces;
                String productType;
                String[] columns;

                while ((columns = csvReader.readNext()) != null) {
                    productSku = getValueByIndex(columns, 0);
                    upc = getValueByIndex(columns, 1);
                    title = getValueByIndex(columns, 2);
                    name = getValueByIndex(columns, 2);
                    description = getValueByIndex(columns, 3);
                    htmlDescription = getValueByIndex(columns, 3);
                    asin = getValueByIndex(columns, 4);
                    finish = getValueByIndex(columns, 5);
                    sizeShape = getValueByIndex(columns, 6);
                    collection = getValueByIndex(columns, 7);
                    lwh = getValueByIndex(columns, 8);
//                    name = getValueByIndex(columns, 2);
//                    mainCategory = getValueByIndex(columns, 4);
//                    subCategory = getValueByIndex(columns, 5);
//                    chairType = getValueByIndex(columns, 6);
//                    finish = getValueByIndex(columns, 7);
//                    style = getValueByIndex(columns, 9);
//                    pieces = getValueByIndex(columns, 10);
//                    collection = getValueByIndex(columns, 11);
//                    productType = getValueByIndex(columns, 12);


                    if (productSku.isEmpty()) {
                        continue;
                    }

                    System.out.println("Processing SKU: " + productSku);


                    Optional<Product> optionalProduct = productRepository.findBySku(productSku);
                    Product product;


                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                    } else {
                        product = Product.builder()
                                .sku(productSku)
                                .upc(upc)
                                .name(name)
                                .title(title)
                                .localSku(skuGenerator.generateNewSKU(productSku))
                                .asin(asin)
                                .discontinued(false)
                                .shippingMethod("GND")
                                .isDeleted(false)
                                .type("Single")
                                .build();

                        ProductDetail productDetail = product.getProductDetail();
                        if (productDetail == null) productDetail = new ProductDetail();
                        productDetail.setDescription(description);
                        productDetail.setHtmlDescription(htmlDescription);
                        productDetail.setFinish(finish);
                        productDetail.setSizeShape(sizeShape);
                        productDetail.setCollection(collection);

                        product.setProductDetail(productDetail);

                        Dimension dimension = product.getDimension();
                        if (dimension == null) dimension = new Dimension();
                        dimension.setLwh(lwh);

                        product.setDimension(dimension);

                        ProductWholesales productWholesales = product.getWholesales();
                        if (productWholesales == null) productWholesales = new ProductWholesales();
                        productWholesales.setEwfmain(true);
                        productWholesales.setEwfdirect(true);

                        product.setWholesales(productWholesales);

                        productRepository.save(product);
                        System.out.println("Inserted new SKU: " + productSku);
                    }

                    ProductDetail productDetail = product.getProductDetail();
                    if (productDetail == null) productDetail = new ProductDetail();
                    if (description.length() > 200) productDetail.setDescription(description);
                    if (htmlDescription.length() > 200) productDetail.setHtmlDescription(description);
//                    if(!upc.isEmpty()) product.setUpc(upc);
//                    if(!name.isEmpty()) product.setName(name);
//                    if(!description.isEmpty()) productDetail.setDescription(description);
//                    if(!mainCategory.isEmpty()) productDetail.setMainCategory(mainCategory);
//                    if(!subCategory.isEmpty()) productDetail.setSubCategory(subCategory);
//                    if (!chairType.isEmpty()) productDetail.setChairType(chairType);
//                    if (!finish.isEmpty()) productDetail.setFinish(finish);
//                    if (!style.isEmpty()) productDetail.setStyle(style);
//                    if (!collection.isEmpty()) productDetail.setCollection(collection);
//                    if (!pieces.isEmpty()) productDetail.setPieces(pieces);
//                    if (!productType.isEmpty()) productDetail.setProductType(productType);

//                    if (!sizeShape.isEmpty()) {
//                        Dimension dimension = product.getDimension();
//                        if (dimension == null) {
//                            dimension = new Dimension();
//                        }
//                        dimension.setSizeShape(sizeShape);
//                        product.setDimension(dimension);
//                    }


                    product.setProductDetail(productDetail);
                    productRepository.save(product);
                    System.out.println("Saved product " + productSku);

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
        System.out.println("Column " + index + " value: " + value);
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
        try (InputStream file = getClass().getResourceAsStream("/data/new_price.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length < 2) {
                    continue;
                }

                String sku = columns[0].trim().toUpperCase();
                double qb2025 = Double.parseDouble(columns[1].trim());

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
                        price.setQB2025(qb2025);
                        product.setPrice(price);
                        productRepository.save(product);
                        System.out.println("Successfully Updated product : " + sku + " VALUES : " + price);
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
