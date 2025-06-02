package com.danny.ewf_service.controller;

import com.danny.ewf_service.converter.IOrderMapper;
import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;

    private final IOrderMapper orderMapper;

    @GetMapping("")
    public ResponseEntity<?> getOrders(@RequestParam(defaultValue = "0") int page) {
        try {
            page -= 1;
            Page<Order> orderPage = orderService.getOrdersByPageAndSort(page);
            PagingResponse<OrderListResponseDto> response = new PagingResponse<>(
                    orderMapper.ordersToOrderResponseDTOs(orderPage.getContent()), // Orders
                    orderPage.getNumber(),                                        // Current Page
                    orderPage.getTotalPages(),                                    // Total Pages
                    orderPage.getSize(),                                          // Page Size
                    orderPage.getTotalElements()                                  // Total Elements
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Orders not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
