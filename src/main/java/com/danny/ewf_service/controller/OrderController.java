package com.danny.ewf_service.controller;


import com.danny.ewf_service.exception.ResourceNotFoundException;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/orders")
@RestController
@AllArgsConstructor
public class OrderController {

    @Autowired
    private final OrderService orderService;

    @GetMapping("")
    public ResponseEntity<?> getOrders() {
        try {
            List<OrderListResponseDto> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to retrieve Orders: " + e.getMessage());
        }
    }
}
