package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;
import com.danny.ewf_service.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<?> getProductBySku(@PathVariable String sku) {
        try {
            ProductResponseDto product = productService.findBySku(sku);
            return ResponseEntity.ok().body(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getProducts() {
        try {
            List<ProductDetailResponseDto> products = productService.findAll();
            if (products.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(products);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<ProductSearchResponseDto>> getProductsSearching() {
        try {
            List<ProductSearchResponseDto> products = productService.getAllProductsSearch();
            if (products.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(products);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
