package com.danny.ewf_service.controller;

import com.danny.ewf_service.service.ComponentService;
import com.danny.ewf_service.utils.exports.ImagesExport;
import com.danny.ewf_service.utils.imports.ComponentsImport;
import com.danny.ewf_service.utils.imports.ImagesImport;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/import")
@RestController
@AllArgsConstructor
public class ImportController {
    private final ImagesExport imagesExport;
    private final ComponentsImport componentsImport;
    private final ComponentService componentService;
    private final ImagesImport imagesImport;


    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
//            imagesImport.updateProductImages();
//            imagesExport.exportImagesShopifyMain("ewfmain.csv");
//            componentsImport.checkSingleProduct();
//            imagesImport.updateComponentImages();
//            imagesExport.updateImagesShopifyFromList("houston.csv");
            componentsImport.importReports();
            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
