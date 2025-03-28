package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SKUGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private final ProductRepository productRepository;


    public void importSkus() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;
            int newSkus = 0;
            int existingSkus = 0;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                String productSku = columns[0].trim();

                if (productSku.isEmpty()) {
                    continue;
                }

                Optional<Product> optionalProduct = productRepository.findBySku(productSku);
                if (optionalProduct.isPresent()) {
                    System.out.println("Existing SKU found: " + productSku);
                    existingSkus++;
                } else {
                    Product product = new Product();
                    product.setSku(productSku);
                    product.setLocalSku(generateNewSKU(productSku));
                    productRepository.save(product);
                    System.out.println("Inserted new SKU: " + productSku);
                    newSkus++;
                }
            }
            // Print summary
            System.out.println("\nImport Summary:");
            System.out.println("New SKUs inserted: " + newSkus);
            System.out.println("Existing SKUs found: " + existingSkus);
            System.out.println("Total SKUs processed: " + (newSkus + existingSkus));

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public String generateNewSKU(String originalSKU) {

        int length = originalSKU.length();
        StringBuilder sb;
        boolean existsSku;
        do {
            if (length < 6) {
                // Keep first 2 characters unchanged
                sb = new StringBuilder(originalSKU.substring(0, Math.min(2, length)));

                // Replace characters after position 2
                for (int i = 2; i < length; i++) {
                    char currentChar = originalSKU.charAt(i);

                    if (currentChar == '-') {
                        // Retain "-" character at its current position
                        sb.append(currentChar);
                    } else {
                        // Replace other characters with random characters from ALLOWED_CHARACTERS
                        int randomIndex = RANDOM.nextInt(ALLOWED_CHARACTERS.length());
                        sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
                    }
                }
            } else {
                // Keep first 5 characters unchanged for SKUs with length >= 6
                sb = new StringBuilder(originalSKU.substring(0, Math.min(5, length)));

                // Replace characters from position 5 onwards
                for (int i = 5; i < length; i++) {
                    char currentChar = originalSKU.charAt(i);

                    if (currentChar == '-') {
                        // Retain "-" character at its current position
                        sb.append(currentChar);
                    } else {
                        // Replace other characters with random characters from ALLOWED_CHARACTERS
                        int randomIndex = RANDOM.nextInt(ALLOWED_CHARACTERS.length());
                        sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
                    }
                }
            }

            existsSku = productRepository.existsProductByLocalSku(sb.toString());
        } while (existsSku);

        return sb.toString();
    }


    private String escapeCsvField(String field) {
        if (field == null || field.isEmpty()) {
            return "Unknown Title"; // Return empty text if the field is null or empty
        }

        // Check if the field contains special characters
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Escape double quotes within the field by replacing them with two double quotes
            field = field.replace("\"", "\"\"");
            // Enclose the entire field in double quotes
            return "\"" + field + "\"";
        }
        return field;
    }


}