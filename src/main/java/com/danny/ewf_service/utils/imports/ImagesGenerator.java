package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.utils.GetCellValue;
import com.danny.ewf_service.utils.ImageCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ImagesGenerator {


    @Autowired
    private ImageCheck imageCheck;

    @Autowired
    private final GetCellValue getCellValue;

    public ImagesGenerator(GetCellValue getCellValue) {
        this.getCellValue = getCellValue;
    }

    public void importImagesFromSirv(String filepath){
        String updateImagesSQL = "UPDATE products SET images = ? WHERE sku = ?";

    }

    public void generateImages(String filePath) {
        String updateImagesSQL = "UPDATE products SET images = ? WHERE sku = ?";
//
//        try (Workbook workbook = new XSSFWorkbook(filePath);
//             PreparedStatement updateStmt = connection.prepareStatement(updateImagesSQL)) {
//
//            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
//            int updatedRows = 0;
//            int skippedRows = 0;
//
//            // Iterate through rows (starting from index 1 if there's a header row)
//            for (Row row : sheet) {
//                Cell skuCell = row.getCell(0); // First column: SKU
//                if (skuCell == null) {
//                    skippedRows++;
//                    continue; // Skip if SKU is missing
//                }
//
//                // Get SKU as a string
//                String sku = getCellValue.getCellValueAsString(skuCell).trim();
//                if (sku.isEmpty()) {
//                    skippedRows++;
//                    continue; // Skip if SKU is empty
//                }
//
//                // Process image links
//                List<String> imgLinks = new ArrayList<>();
//                List<String> dimLinks = new ArrayList<>();
//                for (int i = 1; i < row.getLastCellNum(); i++) { // Iterate through all columns starting from the second column
//                    Cell linkCell = row.getCell(i);
//                    if (linkCell == null) continue;
//
//                    String link = getCellValue.getCellValueAsString(linkCell).trim();
//                    if (link.isEmpty() || !imageCheck.isImageLinkAlive(link)) {
//                        continue;
//                    }
//
//                    // Classify links into `img` or `dim`
//                    if (link.contains("/DIM/")) {
//                        dimLinks.add(link);
//                    } else {
//                        imgLinks.add(link);
//                    }
//                }
//
//                // Create JSON format string
//                if (imgLinks.isEmpty() && dimLinks.isEmpty()) {
//                    skippedRows++;
//                    continue; // Skip if no valid links are available
//                }
//                Map<String, List<String>> imageJson = new HashMap<>();
//                imageJson.put("img", imgLinks);
//                imageJson.put("dim", dimLinks);
//                String imagesJsonString = buildJsonString(imageJson);
//
//                // Perform the database update
//                updateStmt.setString(1, imagesJsonString); // Set the JSON
//                updateStmt.setString(2, sku); // Locate the product using SKU
//                System.out.println("Executing update for SKU: " + sku);
//                int affectedRows = updateStmt.executeUpdate();
//
//                if (affectedRows > 0) {
//                    updatedRows++;
//                } else {
//                    skippedRows++;
//                }
//            }
//
//            // Print update summary
//            System.out.println("\nUpdate Summary:");
//            System.out.println("Rows successfully updated: " + updatedRows);
//            System.out.println("Rows skipped (missing data or invalid): " + skippedRows);
//
//        } catch (Exception e) {
//            System.err.println("Error generating images: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    // Helper function: Build JSON string from image links map
    private String buildJsonString(Map<String, List<String>> imageJson) {
        StringBuilder jsonBuilder = new StringBuilder("{");

        // Add 'img' links
        jsonBuilder.append("\"img\": [");
        for (int i = 0; i < imageJson.get("img").size(); i++) {
            jsonBuilder.append("\"").append(imageJson.get("img").get(i)).append("\"");
            if (i < imageJson.get("img").size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("],");

        // Add 'dim' links
        jsonBuilder.append("\"dim\": [");
        for (int i = 0; i < imageJson.get("dim").size(); i++) {
            jsonBuilder.append("\"").append(imageJson.get("dim").get(i)).append("\"");
            if (i < imageJson.get("dim").size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }
}