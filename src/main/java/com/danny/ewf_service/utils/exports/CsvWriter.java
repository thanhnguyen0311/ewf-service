package com.danny.ewf_service.utils.exports;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            for (String[] row : rows) {
                writer.writeNext(row);
                System.out.println(Arrays.toString(row));
            }

            System.out.println("CSV file successfully exported to: " + csvFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while exporting the CSV file.");
        }
    }

    public Set<String> skuListFromCsv(String csvFilePath){
        Set<String> skuSet = new HashSet<>();
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
                    skuSet.add(columns[0].trim().toLowerCase());
                }
            }
            System.out.println("Product count: " + skuSet.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuSet;
    }

}
