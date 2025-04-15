package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import com.danny.ewf_service.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ComponentsImport {

    private final String[] REQUIRED_HEADERS = {
            "ListID", "Group Name", "Is Active", "Description", "Group Line Item Name", "Group Line Item Qty"
    };

    private final String[] REQUIRED_HEADERS_INVENTORY = {
            "", "Item", "Reorder Pt (Min)", "On Hand"
    };
    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final ProductService productService;

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private CacheService cacheService;


    @Transactional
    public void importComponentsFromData() {
        Set<String> columnOneValues = new HashSet<>();
        Set<String> uniqueGroupLineItemNames = new HashSet<>();

        try (InputStream file = getClass().getResourceAsStream("/data/import_components.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;

            // Read the header row
            String headerRow = reader.readLine();
            if (headerRow == null || !validateHeaderRow(headerRow, REQUIRED_HEADERS)) {
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

    private boolean validateHeaderRow(String headerRow, String[] headersRequired) {
        String[] headers = headerRow.split(",");

        if (headers.length != headersRequired.length) {
            return false;
        }

        for (int i = 0; i < headers.length; i++) {
            if (headers[i].isEmpty() || Objects.equals(headersRequired[i], "")) continue;
            if (!Objects.equals(headers[i].trim(), headersRequired[i])) {
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

            if (headerRow == null || !validateHeaderRow(headerRow, REQUIRED_HEADERS)) {
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

                long quantity;
                try {
                    quantity = Long.parseLong(quantityStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quantity for product " + productSku + " and component " + componentSku);
                    continue;
                }

                try {
                    Product product;
                    Optional<Product> optionalProduct = productRepository.findBySku(productSku);
                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                    } else {
                        product = new Product();
                        product.setSku(productSku);
                        cacheService.saveProduct(product);
                        System.out.println("\u001B[32m" + "Successfully created Product SKU : " + productSku + "\u001B[0m");
                    }

                    Component component = componentRepository.findBySku(componentSku)
                            .orElseThrow(() -> new RuntimeException("Component not found: " + componentSku));

                    boolean mappingExists = productComponentRepository.findByProductIdAndComponentId(product.getId(), component.getId()).isPresent();

                    if (mappingExists) {
                        System.out.println("Mapping already exists between Product: " + productSku + " and Component: " + componentSku);
                        continue;
                    }

                    ProductComponent productComponent = ProductComponent.builder()
                            .product(product)
                            .component(component)
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

    @Transactional
    public void importComponentsInventory() {
        try (InputStream file = getClass().getResourceAsStream("/data/item_inventory.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;
            String headerRow = reader.readLine();

            if (headerRow == null || !validateHeaderRow(headerRow, REQUIRED_HEADERS_INVENTORY)) {
                throw new RuntimeException("Invalid CSV format");
            }

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length < 4) {
                    continue;
                }

                String componentSku = columns[1].trim();  // Column 2: Product SKU
                String quantity = columns[3].trim(); // Column 4: Component SKU


                if (componentSku.isEmpty() || quantity.isEmpty()) {
                    continue;
                }

                if (componentSku.equalsIgnoreCase("20-DEC") || componentSku.equals("46011") || componentSku.equals("12/20/2025"))
                    componentSku = "DEC-20";
                if (componentSku.equals("Total Inventory")) return;
                try {
                    Component component;
                    Optional<Component> optionalComponent = componentRepository.findBySku(componentSku);
                    if (optionalComponent.isPresent()) {
                        component = optionalComponent.get();
                        System.out.println("Successfully Updated Component SKU : " + componentSku);
                    } else {
                        component = new Component();
                        component.setSku(componentSku);
                        System.out.println("\u001B[32m" + "Successfully created Component SKU : " + componentSku + "\u001B[0m");
                    }
                    component.setInventory((long) Math.ceil(Double.parseDouble(quantity)));
                    componentRepository.save(component);
                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + componentSku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    public void checkSingleProduct() {
        List<Component> componentList = componentRepository.findAll();
        Product product;
        for (Component component : componentList) {
            Optional<Product> optionalProduct = productRepository.findBySku(component.getSku());
            if (optionalProduct.isPresent()) {
                component.setType("Single");
                componentRepository.save(component);
                boolean mappingExists = productComponentRepository.findByProductIdAndComponentId(optionalProduct.get().getId(), component.getId()).isPresent();
                if (mappingExists) {
                    System.out.println("Mapping already exists between Product: " + optionalProduct.get().getSku() + " and Component: " + component.getSku());
                    continue;
                }
                ProductComponent productComponent = ProductComponent.builder()
                        .product(optionalProduct.get())
                        .component(component)
                        .quantity(1L)
                        .build();
                productComponentRepository.save(productComponent);
            } else {
                component.setType("Group");
                componentRepository.save(component);
            }
        }
    }

    public void importReports() {
        try (InputStream file = getClass().getResourceAsStream("/data/report.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                String componentSku = columns[0].trim();  // Column 2: Product SKU
                String quantity;
                // Column 4: Component SKU
                if (columns.length < 2) {
                    quantity = "0";
                } else {
                    quantity = columns[1].trim();
                }

                if (componentSku.isEmpty()) {
                    continue;
                }

                if (componentSku.equalsIgnoreCase("20-DEC") || componentSku.equals("46011") || componentSku.equals("12/20/2025"))
                    componentSku = "DEC-20";

                try {
                    Component component;
                    Optional<Component> optionalComponent = componentRepository.findBySku(componentSku);
                    if (optionalComponent.isPresent()) {
                        component = optionalComponent.get();
//                        component.getReport().setInProduction((long) Math.ceil(Double.parseDouble(quantity)));
                        component.setDiscontinue(true);
                        componentRepository.save(component);
                        System.out.println("Successfully Updated Component SKU : " + componentSku + " VALUES : " + quantity);
                    }
                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + componentSku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    @Transactional
    public void importDimensions() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            Set<String> componentSkus = new HashSet<>();
            String line;

            while ((line = reader.readLine()) != null) {

                String[] columns = line.split(",");
                if (columns.length < 1) {
                    continue;
                }

                String componentSku = columns[0].trim();

                if (componentSku.isEmpty() || componentSkus.contains(componentSku)) {
                    continue;
                }

                if (componentSku.equalsIgnoreCase("20-DEC") || componentSku.equals("46011") || componentSku.equals("12/20/2025"))
                    componentSku = "DEC-20";

                try {
                    Component component;
                    Optional<Component> optionalComponent = componentRepository.findBySku(componentSku);
                    if (optionalComponent.isPresent()) {
                        component = optionalComponent.get();

                    } else {
                        component = new Component();
                        component.setSku(componentSku);
                    }
                    component.setPos(5L);
                    componentRepository.save(component);
                    componentSkus.add(componentSku);
                    System.out.println("Successfully Updated Component SKU : " + componentSku + " VALUES : " + component.getPos());


                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + componentSku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    @Transactional
    public void importPrices() {
        try (InputStream file = getClass().getResourceAsStream("/data/skus.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {

            Set<String> componentSkus = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                String sku = columns[0].trim();
                double QB1 = Double.parseDouble(columns[1].trim());
                double QB2 = Double.parseDouble(columns[2].trim());
                double QB3 = Double.parseDouble(columns[3].trim());
                double QB4 = Double.parseDouble(columns[4].trim());
                double QB5 = Double.parseDouble(columns[5].trim());
                double QB6 = Double.parseDouble(columns[6].trim());
                double QB7 = Double.parseDouble(columns[7].trim());

                if (sku.isEmpty() || componentSkus.contains(sku)) {
                    continue;
                }

                if (sku.equalsIgnoreCase("20-DEC") || sku.equals("46011") || sku.equals("12/20/2025")) sku = "DEC-20";

                try {
                    Product product;
                    Component component;
                    Optional<Component> optionalComponent = componentRepository.findBySku(sku);
                    if (optionalComponent.isPresent()) {
                        component = optionalComponent.get();
                        if (component.getPrice() == null) {
                            Price price = Price.builder().QB1(QB1).QB2(QB2).QB3(QB3).QB4(QB4).QB5(QB5).QB6(QB6).QB7(QB7).build();
                            component.setPrice(price);
                            componentRepository.save(component);
                            System.out.println("Successfully Updated Component SKU : " + sku + " VALUES : " + price.toString());
                        }
                    }
                    Optional<Product> optionalProduct = productRepository.findBySku(sku);
                    if (optionalProduct.isPresent()) {
                        product = optionalProduct.get();
                        if (product.getPrice() == null) {
                            Price price = Price.builder().QB1(QB1).QB2(QB2).QB3(QB3).QB4(QB4).QB5(QB5).QB6(QB6).QB7(QB7).build();
                            product.setPrice(price);
                            productRepository.save(product);
                            System.out.println("Successfully Updated Product SKU : " + sku + " VALUES : " + price.toString());
                        }
                    }
                    componentSkus.add(sku);
                } catch (RuntimeException e) {
                    System.err.println("Error processing row for component " + sku + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading CSV file", e);
        }
    }
}
