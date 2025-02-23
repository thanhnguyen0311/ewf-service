package com.danny.ewf_service.utils.imports;

import com.danny.ewf_service.entity.*;
import com.danny.ewf_service.payload.request.OrderImportDto;
import com.danny.ewf_service.repository.CustomerRepository;
import com.danny.ewf_service.repository.OrderRepository;
import com.danny.ewf_service.repository.ProductRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderImport {

    @Autowired
    private final GetCellValue getCellValue;

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final CustomerRepository customerRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Transactional
    public void importFromExcel() {
        List<OrderImportDto> orders = new ArrayList<>();

        try (InputStream file = getClass().getResourceAsStream("/data/Book1.xlsx");
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0); // Reading the first sheet
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Start from row 2 (index 1)
                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                // Extract cell values
                String type = getCellValue.getCellValueAsString(row.getCell(0));
                if (type != null && type.contains("DirectSales")) type = "direct";
                String phone = getCellValue.getCellValueAsString(row.getCell(21))
                        .replaceAll("\\D", ""); // Remove non-numeric characters;
                String invoiceNumber = getCellValue.getCellValueAsString(row.getCell(1));
                String PONumber = getCellValue.getCellValueAsString(row.getCell(3));
                String orderDate = getCellValue.getCellValueAsString(row.getCell(4));
                String carrier = getCellValue.getCellValueAsString(row.getCell(5));
                String tracking = getCellValue.getCellValueAsString(row.getCell(6));
                String shipDate = getCellValue.getCellValueAsString(row.getCell(8));
                Long quantity = getCellValue.getCellValueAsLong(row.getCell(9));
                String sku = getCellValue.getCellValueAsString(row.getCell(10));
                Double price = getCellValue.getCellValueAsDouble(row.getCell(11));
                Double priceCheck = getCellValue.getCellValueAsDouble(row.getCell(14));

                // Map to DTO
                OrderImportDto order = new OrderImportDto();
                order.setOrderDate(orderDate);
                order.setCarrier(carrier);
                order.setPrice(price);
                order.setInvoiceNumber(invoiceNumber);
                order.setPONumber(PONumber);
                order.setTracking(tracking);
                order.setPhone(phone);
                order.setShipDate(shipDate);
                order.setQuantity(quantity);
                order.setSku(sku);
                order.setType(type);
                order.setPriceCheck(priceCheck);

                orders.add(order);

            }
            saveOrders(orders);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading Excel file", e);
        }
    }
    private void saveOrders(List<OrderImportDto> orders) {

        orders.forEach(orderImportDto -> {
            Optional<Order> optionalOrder = orderRepository.findByInvoiceNumber(orderImportDto.getInvoiceNumber());

            optionalOrder.ifPresentOrElse(
                    existingOrder -> {
                        addProductToOrder(existingOrder, orderImportDto.getSku(), orderImportDto.getQuantity());
                        orderRepository.save(existingOrder);
                    },
                    () -> {
                        Order newOrder = new Order();
                        OrderPrices orderPrices = new OrderPrices();
                        newOrder.setInvoiceNumber(orderImportDto.getInvoiceNumber());
                        newOrder.setOrderDate(parseExcelDate(orderImportDto.getOrderDate()));
                        newOrder.setShipDate(parseExcelDate(orderImportDto.getShipDate()));
                        newOrder.setCarrier(orderImportDto.getCarrier());

                        orderPrices.setPrice(orderImportDto.getPrice());
                        orderPrices.setPriceCheck(orderImportDto.getPriceCheck());
                        newOrder.setOrderPrices(orderPrices);

                        newOrder.addToMetadata("PONumber", orderImportDto.getPONumber());
                        newOrder.addToMetadata("Tracking", orderImportDto.getTracking());
                        List<Customer> customers = customerRepository.findByPhone(orderImportDto.getPhone());
                        if (customers.size() == 1) {
                            Customer customer = customers.get(0);
                            newOrder.setCustomer(customer);
                        }

                        addProductToOrder(newOrder, orderImportDto.getSku(), orderImportDto.getQuantity());

                        orderRepository.save(newOrder);

                        System.out.println("New order created with invoice number: " + orderImportDto.getInvoiceNumber());
                    }
            );
        });
    }


    private void addProductToOrder(Order order, String sku, Long quantity) {
        Optional<Product> optionalProduct = productRepository.findBySkuIgnoreCase(sku);
        optionalProduct.ifPresent(product -> {
            // Check if the product already exists in the order
            Optional<OrderProduct> existingOrderProduct = order.getOrderProducts().stream()
                    .filter(orderProduct -> orderProduct.getProduct().equals(product))
                    .findFirst();

            if (existingOrderProduct.isPresent()) {
                // Update quantity by adding new quantity to the old quantity
                OrderProduct orderProduct = existingOrderProduct.get();
                long updatedQuantity = orderProduct.getQuantity() + quantity;
                orderProduct.setQuantity(updatedQuantity);
            } else {
                // Add new product to the order if it doesn't already exist
                OrderProduct orderProduct = OrderProduct.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .build();
                order.getOrderProducts().add(orderProduct);
            }
        });
    }


    private LocalDateTime parseExcelDate(String input) {
        try {
            // Check if the input is a number (possible Excel serial format)
            if (input.matches("\\d+")) {
                // Convert Excel serial number to LocalDateTime
                int serialDate = Integer.parseInt(input);
                return LocalDateTime.of(1899, 12, 30, 0, 0).plusDays(serialDate);
            } else {
                // Otherwise, parse as ISO-8601 or other valid LocalDateTime formats
                return LocalDateTime.parse(input);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + input, e);
        }
    }


}