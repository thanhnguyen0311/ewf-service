package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.ShopifyOrder;
import com.danny.ewf_service.exception.ResourceNotFoundException;
import com.danny.ewf_service.payload.request.AmzRequestDto;
import com.danny.ewf_service.payload.request.OrderRequestDto;
import com.danny.ewf_service.payload.request.sheet.ShopifyOrderRequestDto;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.service.OrderService;
import com.danny.ewf_service.service.ShopifyOrderService;
import com.danny.ewf_service.service.impl.DfShippingLabelService;
import lombok.AllArgsConstructor;
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

    @PostMapping("/amz")
    public ResponseEntity<?> getAMZOrderDetail(@RequestBody AmzRequestDto amzRequestDto) {
        try {
            List<DfShippingLabelService.LabelDetails> labelDetails = orderService.getLabels(amzRequestDto.getPoNumber(), amzRequestDto.getAccessToken());
            return ResponseEntity.ok(labelDetails);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to retrieve Orders: " + e.getMessage());
        }
    }

    @PostMapping("/amz/order")
    public ResponseEntity<?> updateAMZOrder(List<OrderRequestDto> orderRequestDtos) {
        try {
            orderService.updateAmzOrders(orderRequestDtos);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating Amazon orders: " + e.getMessage());
        }
    }
}
