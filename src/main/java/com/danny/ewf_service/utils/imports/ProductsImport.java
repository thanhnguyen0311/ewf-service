package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.opencsv.CSVReader;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
             BufferedReader reader = new BufferedReader(new InputStreamReader(file));
             CSVReader csvReader = new CSVReader(reader)) {

            String productSku;
            String cat;
            String cat2;
            String item1sku;
            String item2sku;
            String[] columns;

            int newSkus = 0;
            int existingSkus = 0;

            while ((columns = csvReader.readNext()) != null) {
                productSku = getValueByIndex(columns, 0);
                cat = getValueByIndex(columns, 1);
                cat2 = getValueByIndex(columns, 2);

                if (productSku.isEmpty()) {
                    continue;
                }

                Optional<Product> optionalProduct = productRepository.findBySku(productSku);
                Product product;

                long pos = 1;

                if (optionalProduct.isPresent()) {
                    product = optionalProduct.get();
                    existingSkus++;
                } else {
                    product = new Product();
                    product.setSku(productSku);
                    product.setLocalSku(skuGenerator.generateNewSKU(productSku));
                    productRepository.save(product);
                    System.out.println("Inserted new SKU: " + productSku);
                    newSkus++;
                }

                if (!cat.isEmpty()) {
                    product.setCategory(cat);
                }

                if (!cat2.isEmpty()) {
                    product.setCategory2(cat2);
                }

                List<ProductComponent> productComponentList = product.getComponents();
                if (productComponentList == null) {
                    productComponentList = new ArrayList<>();
                }
                if (!productComponentList.isEmpty()) {
                    continue;
                }

                item1sku = getValueByIndex(columns, 3);
                item2sku = getValueByIndex(columns, 4);

                if (!item1sku.isEmpty()) {
                    Optional<Component> optionalComponent = componentRepository.findBySku(item1sku);
                    if (optionalComponent.isPresent()) {
                        Component component = optionalComponent.get();
                        ProductComponent productComponent = new ProductComponent();
                        productComponent.setProduct(product);
                        productComponent.setComponent(component);
                        productComponent.setQuantity(1L);

                        component.setPos(pos);
                        componentRepository.save(component);

                        productComponentRepository.save(productComponent);
                        System.out.println("Inserted component " + component.getSku() + " for product " + product.getSku() + " quantity " + productComponent.getQuantity());
                    }
                }
                pos = 2 ;

                if (!item2sku.isEmpty()) {
                    Optional<Component> optionalComponent = componentRepository.findBySku(item2sku);
                    if (optionalComponent.isPresent()) {
                        Component component = optionalComponent.get();
                        ProductComponent productComponent = new ProductComponent();
                        productComponent.setProduct(product);
                        productComponent.setComponent(component);
                        productComponent.setQuantity(1L);
                        productComponentRepository.save(productComponent);

                        component.setPos(pos);
                        componentRepository.save(component);

                        System.out.println("Inserted component " + component.getSku() + " for product " + product.getSku() + " quantity " + productComponent.getQuantity());
                    }
                }

                for (int i = 5; i <= 17; i = i + 3) {
                    pos++;
                    if (!getValueByIndex(columns, i).isEmpty()) {
                        Optional<Component> optionalComponent = componentRepository.findBySku(getValueByIndex(columns, i));
                        if (optionalComponent.isPresent()) {
                            Component component = optionalComponent.get();
                            ProductComponent productComponent = new ProductComponent();
                            productComponent.setProduct(product);
                            productComponent.setComponent(component);
                            long quantity = Long.parseLong(getValueByIndex(columns, i + 2));
                            if (quantity == 0) {
                                quantity = Long.parseLong(getValueByIndex(columns, i + 1)) * component.getDimension().getQuantityBox();
                            }
                            productComponent.setQuantity(quantity);
                            productComponentRepository.save(productComponent);


                            component.setPos(pos);
                            componentRepository.save(component);

                            System.out.println("Inserted component " + component.getSku() + " for product " + product.getSku() + " quantity " + productComponent.getQuantity());
                        }
                    }
                }
            }
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
                    productWholesales.setEwfmain(true);
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

    public void importProductShopify() {

    }
}
