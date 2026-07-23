package com.danny.ewf_service.service;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.stereotype.Service;

public interface SpreadsheetService {

    Sheets getSheetsService() throws Exception;

    String columnIndexToLetter(int columnIndex);

    void updateProductData(String[] targetHeaders) throws Exception;
}
