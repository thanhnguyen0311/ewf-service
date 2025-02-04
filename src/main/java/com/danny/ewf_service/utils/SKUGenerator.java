package com.danny.ewf_service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;


@Component
public class SKUGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUser;

    @Value("${spring.datasource.password}")
    private String databasePassword;


    public void generateAndUpdateSKUs() {
        // Fetch only rows where local_sku is NULL or empty
        String selectSQL = "SELECT id, sku FROM products WHERE sku IS NOT NULL AND (local_sku IS NULL OR local_sku = '')";
        String updateSQL = "UPDATE products SET local_sku = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
             PreparedStatement updateStmt = connection.prepareStatement(updateSQL)) {

            // Fetch rows where local_sku needs to be updated
            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                int productId = resultSet.getInt("id");
                String originalSKU = resultSet.getString("sku");

                // Generate a new SKU based on the originalSKU
                String newSKU = generateNewSKU(originalSKU);

                // Update the `local_sku` column with the new SKU
                updateStmt.setString(1, newSKU);
                updateStmt.setInt(2, productId);

                // Execute the update
                updateStmt.executeUpdate();
            }

            System.out.println("SKUs successfully generated and updated where local_sku was missing or empty.");

        } catch (Exception e) {
            System.err.println("Error while generating SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateNewSKU(String originalSKU) {
        int length = originalSKU.length();
        StringBuilder sb;

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
        return sb.toString();
    }

    public void wipeLocalSKUs() {
        String wipeSQL = "UPDATE products SET local_sku = NULL";

        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement wipeStmt = connection.prepareStatement(wipeSQL)) {

            int rowsAffected = wipeStmt.executeUpdate();
            System.out.println("Successfully wiped local_sku values in " + rowsAffected + " rows.");

        } catch (Exception e) {
            System.err.println("Error while wiping local_sku values: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SKUGenerator generator = new SKUGenerator();
        generator.databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        generator.databaseUser = "root";
        generator.databasePassword = "2024";
        generator.generateAndUpdateSKUs();
    }

}