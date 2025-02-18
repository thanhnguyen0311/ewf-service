package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.configuration.DatasourceConfig;
import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.service.LocalService;
import com.danny.ewf_service.utils.ImageCheck;
import com.danny.ewf_service.utils.ImageProcessor;
import com.danny.ewf_service.utils.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class TitleGenerator {

    @Autowired
    private final ImageCheck imageCheck;

    @Autowired
    private final OpenAIClient openAIClient;


    @Autowired
    private final DatasourceConfig datasourceConfig;

    @Autowired
    private final ImageProcessor imageProcessor;

    @Autowired
    private final LocalService localService;

    public TitleGenerator(ImageCheck imageCheck, OpenAIClient openAIClient, DatasourceConfig datasourceConfig, ImageProcessor imageProcessor, LocalService localService) {
        this.imageCheck = imageCheck;
        this.openAIClient = openAIClient;
        this.datasourceConfig = datasourceConfig;
        this.imageProcessor = imageProcessor;
        this.localService = localService;
    }

    public void generateLocalTitle() {
        List<LocalProduct> localProductList = localService.getAllLocalProducts();
        String userContent = "";
        boolean isLinkAlive;
        for (LocalProduct localProduct : localProductList){
            if (localProduct.getLocalTitle() != null){
                continue;
            }

            String images = localProduct.getProduct().getImages();
            if (images == null || images.isEmpty() || "[]".equals(images.trim())) {
                continue;
            }


            ImageProcessor.ImageUrls imageUrls = imageProcessor.parseImageJson(localProduct.getProduct().getImages());
            for (String imgLink : imageUrls.getImg()) {
                isLinkAlive = imageCheck.isImageLinkAlive(imgLink);
                userContent = "New SKU: " + localProduct.getLocalSku() + "\nImage URL: " + imgLink;
                if(isLinkAlive) {
                    try {
//                        String imageUrl = "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg";
//                        String prompt = "What's in this image?";

//                        String result = openAIClient.analyzeImage(imageUrl, prompt);
                        String result = openAIClient.generateTitle();
                        System.out.println("Analysis result: " + result);

                        TimeUnit.MINUTES.sleep(5);
                        break;
                    } catch (Exception e) {
                        throw new RuntimeException("Error generating title", e);
                    }
                }
            }
        }
    }

    public void importTitlesFromFile(String filePath) {
        String updateTitleSQL = "UPDATE products SET title = ? WHERE sku = ?";

        try (Connection connection = DriverManager.getConnection(datasourceConfig.getUrl(), datasourceConfig.getUsername(), datasourceConfig.getPassword());
             PreparedStatement updateStatement = connection.prepareStatement(updateTitleSQL);
             BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            int updatedCount = 0;

            // Read the file line by line
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);

                if (columns.length < 2) {
                    System.err.println("Invalid row format, skipping: " + line);
                    continue;
                }

                String sku = columns[0].trim().toUpperCase(); // Convert SKU to uppercase
                String title = columns[1].trim();            // Get Title from the second column

                // Update the title for the given SKU in the database
                updateStatement.setString(1, title);
                updateStatement.setString(2, sku);
                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    updatedCount++;
                } else {
                    System.out.println("No matching SKU found for update: " + sku);
                }
            }

            System.out.println("Total rows updated: " + updatedCount);

        } catch (Exception e) {
            System.err.println("Error while importing titles: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
