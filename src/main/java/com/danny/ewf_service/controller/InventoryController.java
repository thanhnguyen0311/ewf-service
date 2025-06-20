package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.ComponentInventoryRequestDto;
import com.danny.ewf_service.payload.request.product.ProductInventorySearchRequestDto;
import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.product.ProductInventoryResponseDto;
import com.danny.ewf_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/inventory")
@AllArgsConstructor
public class InventoryController {

    @Autowired
    private final InventoryService inventoryService;

    @GetMapping("/components")
    public ResponseEntity<?> getComponentsInventory() {
        try {
            List<ComponentInventoryResponseDto> componentInventoryResponseDtos = inventoryService.findAllComponentsInventory();
            return ResponseEntity.ok(componentInventoryResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProductsInventoryAll() {
        try {
            List<ProductInventoryResponseDto> productInventoryResponseDtos = inventoryService.inventoryProductAll();
            return ResponseEntity.ok(productInventoryResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @PostMapping("/products/search")
    public ResponseEntity<?> findProductInventory(@RequestBody ProductInventorySearchRequestDto productInventorySearchRequestDto) {
        try {
//            PagingResponse<ProductInventoryResponseDto> productInventoryResponseDtoList =
//                    inventoryService.inventoryProductSearchBySku(
//                            productInventorySearchRequestDto.getPage()-1
//                            ,productInventorySearchRequestDto.getSku());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @PutMapping("/components")
    @PreAuthorize("hasAnyAuthority('EDIT_INVENTORY', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateComponentInventory(@RequestBody ComponentInventoryRequestDto componentInventoryRequestDto) {
        try {
            ComponentInventoryResponseDto newComponentInventoryResponseDto = inventoryService.updateComponent(componentInventoryRequestDto);
            return ResponseEntity.ok(newComponentInventoryResponseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @GetMapping("/lpn")
    public ResponseEntity<Long> getLooseInventoryByLPNTagId(@RequestParam String lpnTagId) {
        Long quantity = inventoryService.getLooseInventoryByTagID(lpnTagId);
        return ResponseEntity.ok(quantity);
    }
}
