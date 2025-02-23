package com.danny.ewf_service.utils.imports;


import com.danny.ewf_service.entity.Customer;
import com.danny.ewf_service.payload.request.CustomerImportDto;
import com.danny.ewf_service.repository.CustomerRepository;
import com.danny.ewf_service.utils.GetCellValue;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CustomerImport {
    @Autowired
    private final GetCellValue getCellValue;
    @Autowired
    private final CustomerRepository customerRepository;

    @Transactional
    public void importFromExcel() {
        List<CustomerImportDto> customers = new ArrayList<>();

        try (InputStream file = getClass().getResourceAsStream("/data/Book1.xlsx");
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0); // Reading the first sheet
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Start from row 2 (index 1)
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // Extract cell values
                String name = getCellValue.getCellValueAsString(row.getCell(15)); // Column 16 (index 15)
                String address = getCellValue.getCellValueAsString(row.getCell(16)); // Column 17 (index 16)
                String address2 = getCellValue.getCellValueAsString(row.getCell(17)); // Column 18 (index 17)
                String city = getCellValue.getCellValueAsString(row.getCell(18)); // Column 19 (index 18)
                String state = getCellValue.getCellValueAsString(row.getCell(19)); // Column 20 (index 19)
                String zipcode = getCellValue.getCellValueAsString(row.getCell(20)); // Column 21 (index 20)
                String phone = getCellValue.getCellValueAsString(row.getCell(21)) // Column 22 (index 21)
                        .replaceAll("\\D", ""); // Remove non-numeric characters

                // Map to DTO
                CustomerImportDto customer = new CustomerImportDto();
                customer.setName(name);
                customer.setAddress(address);
                customer.setAddress2(address2);
                customer.setCity(city);
                customer.setState(state);
                customer.setZipcode(zipcode);
                customer.setPhone(phone);

                customers.add(customer);

            }
            saveCustomers(customers);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading Excel file", e);
        }
    }
    private void saveCustomers(List<CustomerImportDto> customers) {

        customers.forEach(customerDTO -> {

            boolean exists = customerRepository.existsByPhone(customerDTO.getPhone());
            if (!exists) {

                Customer customer = new Customer(); // Assume Customer is a JPA Entity
                customer.setName(customerDTO.getName());
                customer.setAddress(customerDTO.getAddress());
                customer.setAddress2(customerDTO.getAddress2());
                customer.setCity(customerDTO.getCity());
                customer.setState(customerDTO.getState());
                customer.setZipCode(customerDTO.getZipcode());
                customer.setPhone(customerDTO.getPhone());

                customerRepository.save(customer); // Persist to database
            }
        });
    }

}