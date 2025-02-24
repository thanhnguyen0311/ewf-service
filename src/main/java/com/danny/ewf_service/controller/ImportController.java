package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.utils.imports.CustomerImport;
import com.danny.ewf_service.utils.imports.OrderImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/import")
@CrossOrigin(origins = "*")
@RestController
public class ImportController {
    @Autowired
    private final OrderImport orderImport;

    public ImportController(OrderImport orderImport) {
        this.orderImport = orderImport;
    }

    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
