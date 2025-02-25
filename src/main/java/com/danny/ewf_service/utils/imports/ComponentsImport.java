package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.entity.ProductComponent;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class ComponentsImport {

    private final String[] REQUIRED_HEADERS = {
            "ListID", "Group Name", "Is Active", "Description", "Group Line Item Name", "Group Line Item Qty"
    };
    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ComponentRepository componentRepository;

    @Transactional
    public void importComponentsFromData() {
        Set<String> columnOneValues = new HashSet<>();
        Set<String> uniqueGroupLineItemNames = new HashSet<>();

        try (InputStream file = getClass().getResourceAsStream("/data/import_components.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            // Read the header row
            String headerRow = reader.readLine();
            if (headerRow == null || !validateHeaderRow(headerRow)) {
                throw new RuntimeException("Invalid CSV format");
            }

            // First, gather all unique values from column 1 (index 0)
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length < 6) {
                    continue;
                }

                String columnOneValue = columns[0].trim();
                if (columnOneValue.isEmpty()) continue;
                columnOneValues.add(columnOneValue);
            }

            // Reset reader to parse the file again, this time to extract column 4 values
            try (InputStream fileAgain = getClass().getResourceAsStream("/data/import_components.csv");
                 BufferedReader readerAgain = new BufferedReader(new InputStreamReader(fileAgain))) {

                // Skip the header
                readerAgain.readLine();

                // Now check column 4 values
                while ((line = readerAgain.readLine()) != null) {
                    String[] columns = line.split(",");

                    if (columns.length < 6) {
                        continue;
                    }

                    String groupLineItemName = columns[4].trim();
                    if (groupLineItemName.isEmpty()) continue;
                    // Check if the groupLineItemName doesn't exist in columnOneValues
                    if (!columnOneValues.contains(groupLineItemName)) {
                        uniqueGroupLineItemNames.add(groupLineItemName);
                    }
                }
            }

            // Save unique groupLineItemNames to the database
            for (String sku : uniqueGroupLineItemNames) {
                if (componentRepository.existsBySku(sku)) {
                    System.out.println("Skipped: " + sku + " (Already exists)");
                    continue; // Skip existing component
                }

                Component component = Component.builder()
                        .sku(sku)
                        .quantity(0L)
                        .box(0L)
                        .inventory(0L)
                        .build();
                componentRepository.save(component);
                System.out.println("Saved: " + sku);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    private boolean validateHeaderRow(String headerRow) {
        String[] headers = headerRow.split(",");

        if (headers.length != REQUIRED_HEADERS.length) {
            return false;
        }

        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].trim().equalsIgnoreCase(REQUIRED_HEADERS[i])) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void importProductComponentMapping() {
        try (InputStream file = getClass().getResourceAsStream("/data/import_components.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;
            String headerRow = reader.readLine();

            if (headerRow == null || !validateHeaderRow(headerRow)) {
                throw new RuntimeException("Invalid CSV format");
            }

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length < 6) {
                    continue; // Skip invalid rows
                }

                String productSku = columns[1].trim();  // Column 2: Product SKU
                String componentSku = columns[4].trim(); // Column 5: Component SKU
                String quantityStr = columns[5].trim(); // Column 6: Quantity
                if (productSku.isEmpty() || componentSku.isEmpty() || quantityStr.isEmpty()) {
                    continue;
                }
                // Parse quantity
                Long quantity;
                try {
                    quantity = Long.parseLong(quantityStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quantity for product " + productSku + " and component " + componentSku);
                    continue;
                }

                try {
                    Product product = productRepository.findBySku(productSku)
                            .orElseThrow(() -> new RuntimeException("Product not found: " + productSku));

                    Component component = componentRepository.findBySku(componentSku)
                            .orElseThrow(() -> new RuntimeException("Component not found: " + componentSku));

                    ProductComponent productComponent = ProductComponent.builder()
                            .productId(product.getId())
                            .componentId(component.getId())
                            .quantity(quantity)
                            .build();

                    productComponentRepository.save(productComponent);

                    System.out.println("Successfully mapped product " + productSku + " to component " + componentSku + " with quantity " + quantity);
                } catch (RuntimeException e) {
                    System.err.println("Error processing row for product " + productSku + " and component " + componentSku + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

}
