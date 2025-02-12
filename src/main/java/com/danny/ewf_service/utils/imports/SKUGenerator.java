package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.configuration.DatasourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.FileWriter;
import java.sql.*;
import java.util.Random;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class SKUGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Autowired
    private final DatasourceConfig datasourceConfig;

    public SKUGenerator(DatasourceConfig datasourceConfig) {
        this.datasourceConfig = datasourceConfig;
    }


    public void importSkus(String filePath){
        String checkSkuSQL = "SELECT COUNT(*) FROM products WHERE sku = ?";
        String insertSkuSQL = "INSERT INTO products (sku) VALUES (?)";

        try (Workbook workbook = new XSSFWorkbook(filePath);
             Connection connection = DriverManager.getConnection(datasourceConfig.getUrl(), datasourceConfig.getUsername(), datasourceConfig.getPassword());
             PreparedStatement checkStmt = connection.prepareStatement(checkSkuSQL);
             PreparedStatement insertStmt = connection.prepareStatement(insertSkuSQL)) {

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            int newSkus = 0;
            int existingSkus = 0;

            // Iterate through each row in the first column
            for (Row row : sheet) {
                Cell cell = row.getCell(0); // Get first cell of the row
                if (cell == null) continue;

                // Get SKU value from cell
                String sku = cell.getStringCellValue().trim();

                if (sku.isEmpty()) continue;

                // Check if SKU exists
                checkStmt.setString(1, sku);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    // SKU doesn't exist, insert it
                    insertStmt.setString(1, sku);
                    insertStmt.executeUpdate();
                    System.out.println("Inserted new SKU: " + sku);
                    newSkus++;
                } else {
                    System.out.println("SKU already exists: " + sku);
                    existingSkus++;
                }
            }

            System.out.println("\nImport Summary:");
            System.out.println("New SKUs inserted: " + newSkus);
            System.out.println("Existing SKUs found: " + existingSkus);
            System.out.println("Total SKUs processed: " + (newSkus + existingSkus));

        } catch (Exception e) {
            System.err.println("Error importing SKUs: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void generateAndUpdateSKUs() {
        String selectSQL = """
            SELECT p.id AS product_id, p.sku AS product_sku, p.local_id, l.id AS local_id, l.local_sku
            FROM products p
            LEFT JOIN local l ON p.local_id = l.id
            WHERE l.local_sku IS NULL OR l.local_sku = ''
    """;

        String insertLocalSQL = "INSERT INTO local (local_sku) VALUES (?)";


        String updateProductsSQL = "UPDATE products SET local_id = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(datasourceConfig.getUrl(), datasourceConfig.getUsername(), datasourceConfig.getPassword());
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
             PreparedStatement insertLocalStmt = connection.prepareStatement(insertLocalSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateProductsStmt = connection.prepareStatement(updateProductsSQL)) {

            // Fetch rows with missing local_sku
            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");       // Product's id
                String productSku = resultSet.getString("product_sku");
                String generatedLocalSKU = generateNewSKU(productSku);
                insertLocalStmt.setString(1, generatedLocalSKU);
                int newLocalSku = insertLocalStmt.executeUpdate();
                int newId = 0;
                if (newLocalSku > 0) {
                    try (ResultSet rs = insertLocalStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            newId = rs.getInt(1);  // Get the generated ID
                            System.out.println("Generated and updated local_sku: " + generatedLocalSKU + " for local_id: " + newId);
                        }
                    }
                } else {
                    System.out.println("Insertion failed, no rows affected.");

                    return;
                }
                if (newId != 0) {
                    updateProductsStmt.setInt(1, newId);
                    updateProductsStmt.setInt(2, productId);
                    updateProductsStmt.executeUpdate();
                }
            }

        } catch (Exception e) {
            System.err.println("Error while generating and syncing Local SKUs: " + e.getMessage());
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