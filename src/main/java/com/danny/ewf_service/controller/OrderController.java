package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.ShopifyOrder;
import com.danny.ewf_service.exception.ResourceNotFoundException;
import com.danny.ewf_service.payload.request.sheet.ShopifyOrderRequestDto;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.service.OrderService;
import com.danny.ewf_service.service.ShopifyOrderService;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/orders")
@RestController
@AllArgsConstructor
public class OrderController {

    @Autowired
    private final OrderService orderService;

    @Autowired
    private final ShopifyOrderService shopifyOrderService;

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

    @GetMapping("/shopify")
    public ResponseEntity<?> getShopifyOrders() {
        try {
            List<ShopifyOrder> orders = shopifyOrderService.getAllShopifyOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to retrieve Orders: " + e.getMessage());
        }
    }

    @PostMapping("/shopify")
    public ResponseEntity<?> updateShopifyOrder(@RequestBody ShopifyOrderRequestDto shopifyOrderRequestDto) {
        try {
            shopifyOrderService.updateShopifyOrder(shopifyOrderRequestDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to retrieve Orders: " + e.getMessage());
        }
    }
}
