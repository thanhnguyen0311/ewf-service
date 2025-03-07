package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import com.danny.ewf_service.utils.exports.ImagesExport;
import com.danny.ewf_service.utils.imports.ComponentsImport;
import com.danny.ewf_service.utils.imports.ImagesImport;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/import")
@RestController
@AllArgsConstructor
public class ImportController {
    private final ImagesExport imagesExport;
    private final ComponentsImport componentsImport;
    private final ImagesImport imagesImport;


    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
//            imagesImport.updateProductImages();
            imagesExport.exportImagesShopifyMain("ewfmain.csv");
//            componentsImport.checkSingleProduct();
            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
