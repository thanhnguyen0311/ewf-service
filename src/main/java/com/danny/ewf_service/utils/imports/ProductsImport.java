package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.product.LocalProduct;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductWholesales;
import com.danny.ewf_service.repository.ProductRepository;
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
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;
            String productSku;
            String shipping;
            String discontinued;
            int newSkus = 0;
            int existingSkus = 0;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                productSku = columns[0].trim();
                shipping = columns[1].trim();
                if (columns.length >= 3){
                    discontinued = columns[2].trim();
                } else {
                    discontinued = "";
                }


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
                    LocalProduct localProduct = new LocalProduct();
                    localProduct.setLocalSku(skuGenerator.generateNewSKU(productSku));
                    product.setLocalProduct(localProduct);
                    System.out.println("Inserted new SKU: " + productSku + ", Local SKU: " + localProduct.getLocalSku() );
                    newSkus++;
                }
                if (!shipping.isEmpty())  product.setShippingMethod(shipping);
                if (discontinued.equals("Discontinued")) {
                    product.setDiscontinued(true);
                }
                productRepository.save(product);
                System.out.println("Saved product sku " + product.getSku() + " Ship " + product.getShippingMethod() + " " + discontinued );
            }
            // Print summary
            System.out.println("\nImport Summary:");
            System.out.println("Total SKUs processed: " + (newSkus + existingSkus));

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void importProductWholesales(){
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
            int notFound = 0;
            String line;
            String productSku;
            String upc;
            String asin;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                productSku = columns[0].trim();
                upc = columns[1].trim();
                asin = columns[2].trim();

                if (productSku.isEmpty()) {
                    continue;
                }

                Optional<Product> optionalProduct = productRepository.findBySku(productSku.toUpperCase());
                Product product;

                if (optionalProduct.isPresent()) {
                    product = optionalProduct.get();
                    product.setUpc(upc);
                    product.setAsin(asin);
                    ProductWholesales productWholesales = product.getWholesales();
                    if (productWholesales == null) {
                        productWholesales = new ProductWholesales();
                    }
                    productWholesales.setAmazon(true);
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
