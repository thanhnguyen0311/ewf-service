package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.utils.ImageCheck;
import com.danny.ewf_service.utils.OpenAIClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@Service
@AllArgsConstructor
public class TitleGenerator {

    @Autowired
    private final ImageCheck imageCheck;

    @Autowired
    private final OpenAIClient openAIClient;



    public void generateLocalTitle() {

    }

    public void importTitlesFromFile(String filePath) {
        String updateTitleSQL = "UPDATE products SET title = ? WHERE sku = ?";

        try (Connection connection = DriverManager.getConnection("","","");
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
