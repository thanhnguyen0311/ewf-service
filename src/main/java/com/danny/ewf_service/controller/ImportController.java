package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.utils.imports.ComponentsImport;
import com.danny.ewf_service.utils.imports.CustomerImport;
import com.danny.ewf_service.utils.imports.OrderImport;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/import")
@RestController
@AllArgsConstructor
public class ImportController {
    @Autowired
    private final ComponentsImport componentsImport;


    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            componentsImport.importProductComponentMapping();
            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
