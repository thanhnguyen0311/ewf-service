package com.danny.ewf_service.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Service;

@Service
public class GetCellValue {

    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC ->
                // Convert numeric value to a string
                    String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA ->
                // Evaluate and return the formula result as a string
                    cell.getCellFormula();
            default -> "";
        };
    }
}
