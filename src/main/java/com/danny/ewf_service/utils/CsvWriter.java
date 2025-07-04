package com.danny.ewf_service.utils;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CsvWriter {

    public void exportToCsv(List<String[]> rows, String outputPath){
        String exportDirectoryPath = "src/main/resources/data/exports/";
        String csvFilePath = exportDirectoryPath + outputPath;

        // Ensure the directory exists
        File directory = new File(exportDirectoryPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        System.out.println("Start Exporting CSV file");
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(csvFilePath), StandardCharsets.UTF_8))) { // Use UTF-8 encoding
            for (String[] row : rows) {
                writer.writeNext(row);
            }


            System.out.println("CSV file successfully exported to: " + csvFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while exporting the CSV file.");
        }
    }

    public List<String> skuListFromCsv(String csvFilePath){
        List<String> skuSet = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length > 0) {
                    if (columns[0].trim().isEmpty()) continue;
                    skuSet.add(columns[0].trim().toUpperCase());
                }
            }
            System.out.println("Product count: " + skuSet.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuSet;
    }

    public Map<String, String> skuTitleListFromCsv(String csvFilePath){
        Map<String, String> csvMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length > 0) {
                    if (columns[0].trim().isEmpty()) continue;
                    csvMap.put(columns[0].trim().toLowerCase(), columns[1]);
                }
            }
            System.out.println("Product count: " + csvMap.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvMap;
    }
}
