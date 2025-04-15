package com.danny.ewf_service.controller;

import com.danny.ewf_service.utils.exports.AmazonDataExport;
import com.danny.ewf_service.utils.exports.ShopifyExport;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/import")
@RestController
@AllArgsConstructor
public class ImportController {
    @Autowired
    private final AmazonDataExport amazonDataExport;

    @Autowired
    private final ShopifyExport shopifyExport;


    @GetMapping("/data")
    public ResponseEntity<?> importData() {
        try {
            String filepath = "amazon-price.csv";
//            amazonDataExport.extractDataFromAmazon();
            shopifyExport.exportShopifyProductsPrice(filepath);

            return ResponseEntity.ok().body("SUCCESS");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
