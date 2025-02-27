package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public ResponseEntity<?> getProductsInventory(@RequestParam(defaultValue = "0") int page) {
        try {
            page -= 1;
            PagingResponse<ProductInventoryResponseDto> productInventoryResponseDtoList = inventoryService.inventoryProductList(page);
            return ResponseEntity.ok(productInventoryResponseDtoList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
