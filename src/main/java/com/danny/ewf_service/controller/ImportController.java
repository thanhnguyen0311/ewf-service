package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.ProductResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RestController
public class ImportController {
    @GetMapping("/import")
    public ResponseEntity<?> getProductBySku() {
        try {

            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
