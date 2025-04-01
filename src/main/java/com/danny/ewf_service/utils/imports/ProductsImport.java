package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductDetail;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.repository.ProductRepository;
import com.opencsv.CSVReader;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;


@Service
@AllArgsConstructor
public class ProductsImport {

    @Autowired
    private final ProductRepository productRepository;

    private final SKUGenerator skuGenerator;

    public void importProductDetails() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file));
             CSVReader csvReader = new CSVReader(reader)) {

            String productSku;
            String description;
            String htmlDescription;
            String mainCategory;
            String subCategory;
            String finish;
            String sizeShape;
            String pieces;
            String collection;
            String productType;
            String[] columns;

            int newSkus = 0;
            int existingSkus = 0;

            while ((columns = csvReader.readNext()) != null) {
                productSku = getValueByIndex(columns, 0);
                description = getValueByIndex(columns, 1);
                htmlDescription = getValueByIndex(columns, 2);
                mainCategory = getValueByIndex(columns, 3);
                subCategory = getValueByIndex(columns, 4);
                finish = getValueByIndex(columns, 5);
                sizeShape = getValueByIndex(columns, 6);
                pieces = getValueByIndex(columns, 7);
                collection = getValueByIndex(columns, 8);
                productType = getValueByIndex(columns, 9);


                if (productSku.isEmpty()) {
                    continue;
                }

                Optional<Product> optionalProduct = productRepository.findBySku(productSku);
                Product product;

                if (optionalProduct.isPresent()) {
                    product = optionalProduct.get();
                    existingSkus++;
                } else {
                    product = new Product();
                    product.setSku(productSku);
                    product.setLocalSku(skuGenerator.generateNewSKU(productSku));
                    System.out.println("Inserted new SKU: " + productSku);
                    newSkus++;
                }

                ProductDetail productDetail = product.getProductDetail();
                System.out.println(productDetail);
                if (productDetail == null) productDetail = new ProductDetail();

                productDetail.setDescription(description);

                productDetail.setHtmlDescription(htmlDescription);

                productDetail.setMainCategory(mainCategory);

                productDetail.setSubCategory(subCategory);

                productDetail.setFinish(finish);

                productDetail.setSizeShape(sizeShape);

                productDetail.setPieces(pieces);

                productDetail.setCollection(collection);

                productDetail.setProductType(productType);

                product.setProductDetail(productDetail);

                productRepository.save(product);

                System.out.println("Saved product sku " + product.getSku());
            }
            // Print summary
            System.out.println("\nImport Summary:");
            System.out.println("Total SKUs processed: " + (newSkus + existingSkus));
            System.out.println("Total SKUs new: " + (newSkus));

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getValueByIndex(String[] array, int index) {
        return index < array.length ? array[index].trim() : "";
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
                    productWholesales.setEwfdirect(true);
                    product.setWholesales(productWholesales);
                    productRepository.save(product);
                } else {
                    System.out.println("Product not found: " + productSku);
                    notFound++;
                }
                System.out.println("Saved product " + productSku);
            }

            System.out.println("Not found " + notFound);

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
