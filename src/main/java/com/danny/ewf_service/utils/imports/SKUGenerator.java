package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.repository.LocalRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.FileWriter;
import java.sql.*;
import java.util.Optional;
import java.util.Random;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SKUGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private final LocalRepository localRepository;



    public void importSkus(String filePath){

        String checkSkuSQL = "SELECT id FROM products WHERE sku = ?";
        String insertSkuSQL = "INSERT INTO products (sku) VALUES (?)";
        String insertLocalSQL = "INSERT INTO local (local_sku) VALUES (?)";
        String updateProductsSQL = "UPDATE products SET local_id = ? WHERE id = ?";
        String checkLocalSkuSQL = "SELECT COUNT(*) FROM local WHERE local_sku = ?";
        String generatedLocalSku;



        try (Workbook workbook = new XSSFWorkbook(filePath);
             Connection connection = DriverManager.getConnection("","","");
             PreparedStatement checkStmt = connection.prepareStatement(checkSkuSQL);
             PreparedStatement checkLocalSkuStmt = connection.prepareStatement(checkLocalSkuSQL);
             PreparedStatement insertSkuStmt = connection.prepareStatement(insertSkuSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertLocalStmt = connection.prepareStatement(insertLocalSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateProductsStmt = connection.prepareStatement(updateProductsSQL)) {

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            int newSkus = 0;
            int existingSkus = 0;

            // Iterate through each row in the first column
            for (Row row : sheet) {
                Cell cell = row.getCell(0); // Get first cell of the row
                if (cell == null) continue;

                String sku = cell.getStringCellValue().trim(); // Get SKU value from cell
                if (sku.isEmpty()) continue;

                // Check if SKU already exists
                checkStmt.setString(1, sku);
                ResultSet checkResult = checkStmt.executeQuery();
                if (checkResult.next()) {
                    // SKU already exists
                    System.out.println("SKU already exists: " + sku);
                    existingSkus++;
                    continue;
                }

                // SKU not found, insert into products table
                insertSkuStmt.setString(1, sku);
                int affectedRows = insertSkuStmt.executeUpdate();
                if (affectedRows == 0) {
                    System.err.println("Failed to insert SKU: " + sku);
                    continue;
                }

                // Get the newly generated product ID
                int productId;
                try (ResultSet generatedKeys = insertSkuStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        productId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain product ID for SKU: " + sku);
                    }
                }

                // Generate new local SKU
                do {
                    // Generate a new local SKU
                    generatedLocalSku = generateNewSKU(sku);

                    // Check if it already exists in the local table
                    checkLocalSkuStmt.setString(1, generatedLocalSku);
                    try (ResultSet rs = checkLocalSkuStmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            break;
                        }
                    }
                } while (true); // Repeat until a unique SKU is found


                // Insert new local SKU into local table
                insertLocalStmt.setString(1, generatedLocalSku);
                affectedRows = insertLocalStmt.executeUpdate();
                if (affectedRows == 0) {
                    System.err.println("Failed to insert local SKU: " + generatedLocalSku);
                    continue;
                }

                // Get the newly generated local ID
                int localId;
                try (ResultSet generatedKeys = insertLocalStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        localId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain local ID for local SKU: " + generatedLocalSku);
                    }
                }

                // Update the products table with the new local ID
                updateProductsStmt.setInt(1, localId);
                updateProductsStmt.setInt(2, productId);
                updateProductsStmt.executeUpdate();

                System.out.println("Inserted new SKU: " + sku + ", Local SKU: " + generatedLocalSku);
                newSkus++;
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
        Optional<LocalProduct> optionalLocalProduct;
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

            optionalLocalProduct = localRepository.findByLocalSku(sb.toString());
        } while (optionalLocalProduct.isPresent());

        return sb.toString();
    }


    public void exportProductPrices() {
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String databaseUser = "root";
        String databasePassword = "2024";

        // SQL query to join products and local tables, fetching sku, price, and title
        String EXPORT_QUERY =
                "SELECT p.sku, l.price, l.local_title " +
                        "FROM products p " +
                        "INNER JOIN local l ON p.local_id = l.id";

        // Name of the CSV file to export
        String csvFileName = "exported_product_prices_with_title.csv";

        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement exportStmt = connection.prepareStatement(EXPORT_QUERY);
             ResultSet resultSet = exportStmt.executeQuery();
             FileWriter writer = new FileWriter(csvFileName)) {

            // Write the header row
            writer.append("sku,price,title\n");

            // Write each row of the result to the CSV file
            while (resultSet.next()) {
                String sku = resultSet.getString("sku").toLowerCase();       // Retrieve SKU and convert to lowercase
                double price = resultSet.getDouble("price");                // Retrieve corresponding price
                String title = resultSet.getString("local_title");          // Retrieve corresponding title

                // Escape and format the title to handle special characters (commas, quotes, etc.)
                String escapedTitle = escapeCsvField(title);

                // Append the row to the CSV file
                writer.append(sku).append(",")
                        .append(Double.toString(price)).append(",")
                        .append(escapedTitle).append("\n");
            }

            System.out.println("Export completed successfully to " + csvFileName);

        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();
        }
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