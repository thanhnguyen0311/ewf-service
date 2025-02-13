package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.configuration.DatasourceConfig;
import com.danny.ewf_service.utils.GetCellValue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@Service
public class UPCGenerator {

    @Autowired
    private final DatasourceConfig datasourceConfig;

    @Autowired
    private final GetCellValue getCellValue;

    public UPCGenerator(DatasourceConfig datasourceConfig, GetCellValue getCellValue) {
        this.datasourceConfig = datasourceConfig;
        this.getCellValue = getCellValue;
    }

    public void importUPCs(String filePath) {
        SKUGenerator skuGenerator = new SKUGenerator(datasourceConfig);
        skuGenerator.importSkus(filePath);

        String updateProductSQL = "UPDATE products SET upc = ? WHERE sku = ?";

        try (Workbook workbook = new XSSFWorkbook(filePath);
             Connection connection = DriverManager.getConnection(datasourceConfig.getUrl(), datasourceConfig.getUsername(), datasourceConfig.getPassword());
             PreparedStatement updateStmt = connection.prepareStatement(updateProductSQL)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            int updatedRows = 0;
            int skippedRows = 0;

            // Iterate through each row in the Excel file (starting from index 1 if there's a header row)
            for (Row row : sheet) {
                Cell skuCell = row.getCell(0); // Column 1: SKU
                Cell upcCell = row.getCell(1); // Column 2: UPC

                if (skuCell == null || upcCell == null) {
                    skippedRows++;
                    continue; // Skip rows with missing SKU or UPC
                }

                // Fetch SKU and UPC values
                String sku = getCellValue.getCellValueAsString(skuCell).trim();

                String upc = getCellValue.getCellValueAsString(upcCell).trim();

                if (sku.isEmpty() || upc.isEmpty()) {
                    skippedRows++;
                    continue; // Skip rows with empty SKU or UPC
                }

                // Perform the update in the database
                updateStmt.setString(1, upc);  // Set UPC in the products table
                updateStmt.setString(2, sku);  // Locate product by SKU
                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    updatedRows++;
                } else {
                    skippedRows++;
                }
            }

            // Print update summary
            System.out.println("\nUpdate Summary:");
            System.out.println("Rows successfully updated: " + updatedRows);
            System.out.println("Rows skipped (missing data or no matching record): " + skippedRows);

        } catch (Exception e) {
            System.err.println("Error updating SKUs and UPCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
