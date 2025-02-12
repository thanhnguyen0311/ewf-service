package com.danny.ewf_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XLSXReader {

    private final String xlsxFile;       // File path to the XLSX file


    private final List<Map<String, String>> images = new ArrayList<>();

    @Getter
    private final List<List<String>> data; // List to store XLSX rows in a List<List<String>>

    // Constructor to initialize the XLSX file
    public XLSXReader(String xlsxFile) {
        this.xlsxFile = xlsxFile;
        this.data = new ArrayList<>();
    }

    // Method to read XLSX file
    public void readXLSX() throws IOException, InvalidFormatException {
        try (FileInputStream fis = new FileInputStream(new File(xlsxFile))) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
//                for (Cell cell : row) {
//                    rowData.add(getCellValue(cell));
//                }
                for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.add(getCellValue(cell)); // Get value for each cell, including blanks
                }
                data.add(rowData);
            }
        }
    }


    // Utility method to get cell value as a String
    private String getCellValue(Cell cell) {
        int columnIndex = cell.getColumnIndex();
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (columnIndex == 4) {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue % 1 == 0) {
                        return Integer.toString((int) numericValue);
                    } else {
                        return Double.toString(numericValue);
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "UNKNOWN";
        }

    }


    private void importProductToDB() {

        // Validate the data has at least 3 rows (IDs, Column Names, and Data)
        if (data.size() < 3) {
            System.err.println("Insufficient data in the XLSX file.");
            return;
        }
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {
            String INSERT_PRODUCT_QUERY = "INSERT INTO products (sku, upc, name, type, sub_type, shipping, category, sub_category, finish, description, html_description, images, pdf, product_type, cat, `order`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertProductStmt = conn.prepareStatement(INSERT_PRODUCT_QUERY);

            // Start reading from the 3rd row (Index = 2)
            for (int i = 2; i < data.size(); i++) {

                List<String> row = data.get(i);
                String imagesJson = createImagesJson(row.get(73), row.get(74), row.get(75), row.get(76), row.get(77), row.get(78));
                insertProductStmt.setString(1, row.get(3) != null ? row.get(3) : ""); // SKU (ID 4, 0-based index 3)
                insertProductStmt.setString(2, row.get(4) != null ? row.get(4) : ""); // UPC (ID 5, 0-based index 4)
                insertProductStmt.setString(3, row.get(5) != null ? row.get(5) : ""); // Name (ID 6, 0-based index 5)
                insertProductStmt.setString(4, "");                               // Type (fixed value)
                insertProductStmt.setString(5, row.get(84) != null ? row.get(84) : ""); // Subtype
                insertProductStmt.setString(6, row.get(8) != null ? row.get(8) : "");  // Shipping (ID 9, 0-based index 8)
                insertProductStmt.setString(7, row.get(82) != null ? row.get(82) : ""); // Category (ID 83, 0-based index 82)
                insertProductStmt.setString(8, row.get(83) != null ? row.get(83) : ""); // Sub-category (ID 84, 0-based index 83)
                insertProductStmt.setString(9, row.get(85) != null ? row.get(85) : ""); // Finish (ID 86, 0-based index 85)
                insertProductStmt.setString(10, row.get(6) != null ? row.get(6) : ""); // Description (ID 7, 0-based index 6)
                insertProductStmt.setString(11, row.get(7) != null ? row.get(7) : ""); // HTML Description (ID 8, 0-based index 7)
                insertProductStmt.setString(12, imagesJson != null ? imagesJson : "[]"); // Images JSON
                insertProductStmt.setString(13, row.get(79) != null ? row.get(79) : ""); // PDF (ID 80, 0-based index 79)
                insertProductStmt.setString(14, row.get(90) != null ? row.get(90) : "");
                insertProductStmt.setString(15, row.get(0) != null ? row.get(0) : "");

                String orderValue = row.get(1);
                if (orderValue == null || orderValue.isEmpty()) {
                    insertProductStmt.setNull(16, java.sql.Types.INTEGER); // Use NULL if no value is provided
                } else {
                    try {
                        int orderInt = (int) Double.parseDouble(orderValue); // Parse 'Double' and cast to 'int'
                        insertProductStmt.setInt(16, orderInt);
                    } catch (NumberFormatException e) {
                        // Log a warning and default to 0 if the value is invalid
                        System.err.println("Invalid 'order' value at row " + (i + 1) + ": " + orderValue);
                        insertProductStmt.setInt(16, 0); // Set to default
                    }
                }

                insertProductStmt.addBatch(); // Add the row to the batch
            }

            insertProductStmt.executeBatch(); // Execute batch insert

            System.out.println("Data successfully imported into the database!");
        } catch (Exception e) {
            System.err.println("Error during MySQL import: " + e.getMessage());
        }
    }

    private String createImagesJson(String id74, String id75, String id76, String id77, String id78, String id79) {
        try {

            images.clear();

            if (id74 != null && !id74.isEmpty()) {
                Map<String, String> img1 = new HashMap<>();
                img1.put("img1", id74);
                images.add(img1);
            }

            if (id75 != null && !id75.isEmpty()) {
                Map<String, String> img2 = new HashMap<>();
                img2.put("img2", id75);
                images.add(img2);
            }

            if (id76 != null && !id76.isEmpty()) {
                Map<String, String> img3 = new HashMap<>();
                img3.put("img3", id76);
                images.add(img3);
            }

            if (id77 != null && !id77.isEmpty()) {
                Map<String, String> img4 = new HashMap<>();
                img4.put("img4", id77);
                images.add(img4);
            }

            if (id78 != null && !id78.isEmpty()) {
                Map<String, String> dim1 = new HashMap<>();
                dim1.put("dim1", id78);
                images.add(dim1);
            }

            if (id79 != null && !id79.isEmpty()) {
                Map<String, String> dim2 = new HashMap<>();
                dim2.put("dim2", id79);
                images.add(dim2);
            }

            // Convert to JSON String using Jackson ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            System.err.println("Error while creating images JSON: " + e.getMessage());
        }
        return "[]"; // Return empty JSON array in case of errors
    }


    private void importComponentToDB() {
        // Database connection details
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Validate that there is enough data to process
        if (data.size() < 3) {
            System.err.println("Insufficient data in the XLSX file.");
            return;
        }

        // Start processing from the third row (skipping the first two rows)
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {
            // Query to check for duplicate SKU
            String CHECK_SKU_QUERY = "SELECT COUNT(*) FROM components WHERE sku = ?";

            // Query to insert into "components" table
            String INSERT_COMPONENT_QUERY = "INSERT INTO components (sku, type, quantity, box, dims, box_dims, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
            PreparedStatement checkSkuStmt = conn.prepareStatement(CHECK_SKU_QUERY);
            PreparedStatement insertComponentStmt = conn.prepareStatement(INSERT_COMPONENT_QUERY);

            // Loop through each row in the dataset (starting from the third row)
            for (int i = 2; i < data.size(); i++) {
                List<String> row = data.get(i);

                // Loop through the 8 items in columns 9 to 72
                for (int j = 9; j < 73; j += 8) {
                    try {
                        String sku = row.get(j);                 // Column 9 + (0 mod 8)
                        String type = row.get(j + 1);           // Type - Column 10
                        String quantity = row.get(j + 2);       // Quantity - Column 11
                        String box = row.get(j + 3);            // Box Quantity - Column 12
                        String dims = row.get(j + 4);           // Dims - Column 13
                        String boxDims = row.get(j + 5);        // Box Dims - Column 14

                        // Skip blank SKUs
                        if (sku == null || sku.isEmpty()) {
                            continue;
                        }

                        // Check for duplicate SKUs
                        checkSkuStmt.setString(1, sku);
                        ResultSet rs = checkSkuStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("Duplicate SKU found, skipping: " + sku);
                            continue;
                        }

                        // Add values to the insert statement
                        insertComponentStmt.setString(1, sku);                      // SKU
                        insertComponentStmt.setString(2, type != null ? type : ""); // Type
                        insertComponentStmt.setLong(3, parseLongWithDefault(quantity, 0)); // Quantity
                        insertComponentStmt.setLong(4, parseLongWithDefault(box, 0));      // Box Quantity
                        insertComponentStmt.setString(5, dims != null ? dims : ""); // Dims
                        insertComponentStmt.setString(6, boxDims != null ? boxDims : ""); // Box Dims

                        // Add to batch
                        insertComponentStmt.addBatch();
                    } catch (Exception e) {
                        System.err.println("Error while processing row " + (i + 1) + ", column " + (j + 1) + ": " + e.getMessage());
                    }
                }
            }

            // Execute the batch
            insertComponentStmt.executeBatch();
            System.out.println("Data successfully imported into the components table!");
        } catch (Exception e) {
            System.err.println("Error during database import: " + e.getMessage());
        }
    }


    private Long parseLongWithDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void importComponentDetailToDB() {
        // Database connection details
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        if (data.size() < 3) {
            System.err.println("Insufficient data in the XLSX file.");
            return;
        }

        // Start processing from the third row (skipping the first two rows)
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {
            // Query to check for duplicate SKU
            String CHECK_SKU_QUERY = "SELECT COUNT(*) FROM components WHERE sku = ?";

            // Query to insert into "components" table
            String UPDATE_COMPONENT_QUERY = "UPDATE components SET finish = ?, size_shape = ?, updated_at = NOW() WHERE sku = ? ";
            PreparedStatement checkSkuStmt = conn.prepareStatement(CHECK_SKU_QUERY);
            PreparedStatement insertComponentStmt = conn.prepareStatement(UPDATE_COMPONENT_QUERY);

            // Loop through each row in the dataset (starting from the third row)
            for (int i = 1; i < data.size(); i++) {
                List<String> row = data.get(i);
                try {
                    String sku = row.get(0);
                    String finish = row.get(3);
                    String sizeShape = row.get(4);
                    System.out.println("SKU: " + sku + " | Finish : " + finish + " | Size Shape : " + sizeShape);
                    if (sku == null || sku.isEmpty()) {
                        continue;
                    }
                    checkSkuStmt.setString(1, sku);
                    ResultSet rs = checkSkuStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        insertComponentStmt.setString(1, finish != null ? finish : "");
                        insertComponentStmt.setString(2, sizeShape != null ? sizeShape : "");
                        insertComponentStmt.setString(3, sku);
                        insertComponentStmt.addBatch();
                        System.out.println("Added: " + sku + " | Finish : " + finish + " | Size Shape : " + sizeShape);
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            // Execute the batch
            insertComponentStmt.executeBatch();
            System.out.println("Data successfully imported into the components table!");
        } catch (Exception e) {
            System.err.println("Error during database import: " + e.getMessage());
        }
    }

    private void filterDuplicateSKUs() {
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)) {

            // Step 1: Create a temporary table with the list of rows to keep
            String CREATE_TEMP_TABLE_QUERY =
                    "CREATE TEMPORARY TABLE temp_components AS " +
                            "SELECT MIN(id) AS id " +
                            "FROM components " +
                            "GROUP BY sku";
            try (PreparedStatement createTempTableStmt = conn.prepareStatement(CREATE_TEMP_TABLE_QUERY)) {
                createTempTableStmt.executeUpdate();
            }

            // Step 2: Delete duplicate rows from the 'components' table, excluding the ones in the temporary table
            String DELETE_DUPLICATES_QUERY =
                    "DELETE FROM components " +
                            "WHERE id NOT IN (SELECT id FROM temp_components)";
            try (PreparedStatement deleteDuplicatesStmt = conn.prepareStatement(DELETE_DUPLICATES_QUERY)) {
                int rowsDeleted = deleteDuplicatesStmt.executeUpdate();
                System.out.println("Successfully deleted " + rowsDeleted + " duplicate rows from the components table.");
            }

            // Step 3: Drop the temporary table (optional, it will drop automatically when the session ends)
            String DROP_TEMP_TABLE_QUERY = "DROP TEMPORARY TABLE IF EXISTS temp_components";
            try (PreparedStatement dropTempTableStmt = conn.prepareStatement(DROP_TEMP_TABLE_QUERY)) {
                dropTempTableStmt.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("Error while removing duplicate SKUs: " + e.getMessage());
        }
    }


    private void addComponentsProductRelationshiptoDB() {
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Queries
        String GET_PRODUCT_ID_QUERY = "SELECT id FROM products WHERE sku = ?";
        String GET_COMPONENT_ID_QUERY = "SELECT id FROM components WHERE sku = ?";
        String INSERT_RELATIONSHIP_QUERY = "INSERT INTO product_components (product_id, component_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement getProductIdStmt = conn.prepareStatement(GET_PRODUCT_ID_QUERY);
             PreparedStatement getComponentIdStmt = conn.prepareStatement(GET_COMPONENT_ID_QUERY);
             PreparedStatement insertRelationshipStmt = conn.prepareStatement(INSERT_RELATIONSHIP_QUERY)) {

            // Loop through each row in the dataset (starting from the third row, so index = 2)
            for (int i = 2; i < data.size(); i++) {
                List<String> row = data.get(i);

                try {
                    // Step 1: Get the SKU from column 3 and query product ID
                    String productSKU = row.get(3); // Column 3 (0-indexed)
                    if (productSKU == null || productSKU.isEmpty()) {
                        System.out.println("can't find "  + productSKU + " in products table");
                        continue; // Skip rows without a product SKU
                    }
                    getProductIdStmt.setString(1, productSKU);
                    ResultSet productResultSet = getProductIdStmt.executeQuery();

                    Integer productId = null;
                    if (productResultSet.next()) {
                        productId = productResultSet.getInt("id");
                    } else {
                        System.err.println("Product SKU not found in database: " + productSKU);
                        continue; // Skip if the product SKU is not found
                    }

                    // Step 2: Get component SKUs from columns 9 and 17 and query their IDs
                    String componentSKU1 = row.get(9); // Column 9 (0-indexed)
                    String componentSKU2 = row.get(17); // Column 17 (0-indexed)

                    List<Integer> componentIds = new ArrayList<>();
                    for (String componentSKU : new String[]{componentSKU1, componentSKU2}) {
                        if (componentSKU == null || componentSKU.isEmpty()) {
                            continue; // Skip empty component SKUs
                        }
                        getComponentIdStmt.setString(1, componentSKU);
                        ResultSet componentResultSet = getComponentIdStmt.executeQuery();
                        if (componentResultSet.next()) {
                            componentIds.add(componentResultSet.getInt("id"));
                        } else {
                            System.err.println("Component SKU not found in database: " + componentSKU);
                        }
                    }

                    // Step 3: Insert product-component relationships into the database
                    for (Integer componentId : componentIds) {
                        insertRelationshipStmt.setInt(1, productId); // Product ID
                        insertRelationshipStmt.setInt(2, componentId); // Component ID
                        insertRelationshipStmt.addBatch(); // Add to batch for efficient database execution
                    }

                } catch (Exception e) {
                    System.err.println("Error processing row " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Execute the batch insert for relationships
            insertRelationshipStmt.executeBatch();
            System.out.println("Product-component relationships successfully added to the database.");

        } catch (Exception e) {
            System.err.println("Error while adding product-component relationships to the database: " + e.getMessage());
        }
    }

    private void addProductPrices() {
        String databaseUser = "root";
        String databasePassword = "2024";
        String databaseUrl = "jdbc:mysql://localhost:3306/ewf?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Query to find the `local_sku` in the `local` table using the `sku` in the `products` table
        String FIND_LOCAL_SKU_QUERY =
                "SELECT l.local_sku " +
                        "FROM products p " +
                        "INNER JOIN local l ON l.id = p.local_id " +
                        "WHERE p.sku = ?";

        // Query to update the `price` in the `local` table
        String UPDATE_LOCAL_PRICE_QUERY =
                "UPDATE local SET price = ? WHERE local_sku = ?";

        // Ensure the XLSX file has rows (data)
        if (data.size() < 2) {
            System.err.println("Insufficient data in the XLSX file.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement findLocalSkuStmt = conn.prepareStatement(FIND_LOCAL_SKU_QUERY);
             PreparedStatement updateLocalPriceStmt = conn.prepareStatement(UPDATE_LOCAL_PRICE_QUERY)) {

            for (int i = 0; i < data.size(); i++) { // Skip header row (row 0) for XLSX
                List<String> row = data.get(i);

                try {
                    // Read SKU and Price from XLSX row
                    String sku = row.get(0);   // Column 1: SKU
                    String price = row.get(1); // Column 2: Price

                    if (sku == null || sku.isEmpty() || price == null || price.isEmpty()) {
                        System.out.println("Skipped invalid or empty row: SKU or Price missing.");
                        continue; // Skip invalid rows
                    }

                    // Step 1: Use SKU to find the corresponding `local_sku`
                    findLocalSkuStmt.setString(1, sku); // Match SKU in `products`
                    ResultSet rs = findLocalSkuStmt.executeQuery();

                    if (rs.next()) {
                        // Found a matching local_sku
                        String localSku = rs.getString("local_sku");

                        // Step 2: Update the price in the `local` table
                        updateLocalPriceStmt.setDouble(1, Double.parseDouble(price)); // Set the new price
                        updateLocalPriceStmt.setString(2, localSku); // Targeted `local_sku`
                        int rowsUpdated = updateLocalPriceStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            System.out.println("Updated price for Local SKU: " + localSku);
                        } else {
                            System.out.println("Failed to update price for Local SKU: " + localSku);
                        }
                    } else {
                        // No matching row found for the SKU in the `products` table
                        System.out.println("SKU not found in products table for row: " + sku);
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Invalid price format in row: " + (i + 1) + " - " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error processing row: " + (i + 1) + " - " + e.getMessage());
                }
            }

            System.out.println("Local table prices updated successfully!");

        } catch (Exception e) {
            System.err.println("Error while updating local table: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {

//            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/product-sheet.xlsx");
//            xlsxReader.readXLSX();
//            xlsxReader.importProductToDB();

//            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/product-all.xlsx");
//            xlsxReader.readXLSX();
//            xlsxReader.importComponentDetailToDB();

//            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/product-all.xlsx");
//            xlsxReader.readXLSX();
//            xlsxReader.filterDuplicateSKUs();


            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/data_prices.xlsx");
            xlsxReader.readXLSX();
            xlsxReader.addProductPrices();

        } catch (IOException | InvalidFormatException e) {
            System.err.println("Error occurred while reading the XLSX file: " + e.getMessage());
        }
    }
}