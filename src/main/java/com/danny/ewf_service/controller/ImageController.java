package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.payload.request.product.ProductImageRequestDto;
import com.danny.ewf_service.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/image")
@RestController
@AllArgsConstructor
public class ImageController {

    @Autowired
    private final ImageService imageService;

    @PutMapping("/product")
    @PreAuthorize("hasAnyAuthority('EDIT_PRODUCT', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateProductDetailById(@RequestBody ProductImageRequestDto productImageRequestDto) {
        try {
            ImageUrls images = imageService.updateProductImages(productImageRequestDto);
            return ResponseEntity.ok().body(images);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
