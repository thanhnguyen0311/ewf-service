package com.danny.ewf_service.utils;

import lombok.Getter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XLSXReader {

    private final String xlsxFile;       // File path to the XLSX file

    @Getter
    private final List<List<String>> data; // List to store XLSX rows in a List<List<String>>

    // Constructor to initialize the XLSX file
    public XLSXReader(String xlsxFile) {
        this.xlsxFile = xlsxFile;
        this.data = new ArrayList<>();
    }

    // Method to read XLSX file
    public void readXLSX() throws IOException, InvalidFormatException {
        // Open the file as a FileInputStream
        try (FileInputStream fis = new FileInputStream(new File(xlsxFile))) {
            Workbook workbook = WorkbookFactory.create(fis); // Create a Workbook instance
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet (0 index)

            // Iterate through each row of the sheet
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                // Iterate through each cell in the row
                for (Cell cell : row) {
                    // Convert the cell to a string and add it to rowData
                    rowData.add(getCellValue(cell));
                }
                // Add the row to the data list
                data.add(rowData);
            }
        }
    }

    // Utility method to get cell value as a String
    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Convert date to string
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue % 1 == 0) {
                        return Integer.toString((int) numericValue); // Return as integer
                    } else {
                        return Double.toString(numericValue); // Return as double string (for non-integers)
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

    // Method to print the data read from the XLSX file
    public void printXLSXData() {
        for (List<String> row : data) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println(); // Move to the next row
        }
    }

    public static void main(String[] args) {
        try {
            XLSXReader xlsxReader = new XLSXReader("src/main/resources/data/all-items.xlsx");
            xlsxReader.readXLSX();
            xlsxReader.printXLSXData();
        } catch (IOException | InvalidFormatException e) {
            System.err.println("Error occurred while reading the XLSX file: " + e.getMessage());
        }
    }
}