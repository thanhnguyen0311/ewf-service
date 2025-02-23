package com.danny.ewf_service.controller;

import com.danny.ewf_service.converter.IOrderMapper;
import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;

    private final IOrderMapper orderMapper;

    @GetMapping("")
    public ResponseEntity<?> getOrders(@RequestParam(defaultValue = "0") int page) {
        try {
            Page<Order> orderPage = orderService.getOrdersByPageAndSort(page);
            return ResponseEntity.ok(orderMapper.ordersToOrderResponseDTOs(orderPage.getContent()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Orders not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
