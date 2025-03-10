package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.ProductInventorySearchRequestDto;
import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import com.danny.ewf_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/inventory")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/components")
    public ResponseEntity<?> getProductsInventory() {
        try {
            List<ComponentInventoryResponseDto> componentInventoryResponseDtos = inventoryService.findAllComponentsInventory();
            return ResponseEntity.ok(componentInventoryResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @PostMapping("/products/search")
    public ResponseEntity<?> findProductInventory(@RequestBody ProductInventorySearchRequestDto productInventorySearchRequestDto) {
        try {
            PagingResponse<ProductInventoryResponseDto> productInventoryResponseDtoList =
                    inventoryService.inventoryProductSearchBySku(
                            productInventorySearchRequestDto.getPage()-1
                            ,productInventorySearchRequestDto.getSku());
            return ResponseEntity.ok(productInventoryResponseDtoList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
