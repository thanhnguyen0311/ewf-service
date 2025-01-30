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
                for (Cell cell : row) {
                    rowData.add(getCellValue(cell));
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


    private void importDataToDB() {

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


    public static void main(String[] args) {
        try {
            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/product-sheet.xlsx");
            xlsxReader.readXLSX();
            xlsxReader.importDataToDB();
        } catch (IOException | InvalidFormatException e) {
            System.err.println("Error occurred while reading the XLSX file: " + e.getMessage());
        }
    }
}