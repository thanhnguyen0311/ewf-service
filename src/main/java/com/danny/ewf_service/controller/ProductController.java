package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<?> getProductBySku(@PathVariable String sku) {
        try {
            ProductResponseDto product = productService.findBySku(sku);
            System.out.println("SELECT: " + product.getPrice() + product.getImages());
            return ResponseEntity.ok().body(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
