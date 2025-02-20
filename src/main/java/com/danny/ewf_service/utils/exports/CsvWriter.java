package com.danny.ewf_service.utils.exports;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
}
