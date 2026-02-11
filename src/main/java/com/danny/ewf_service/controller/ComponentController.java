package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.product.ProductDetailRequestDto;
import com.danny.ewf_service.payload.response.component.ComponentInboundResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentListWMSResponse;
import com.danny.ewf_service.payload.response.component.ComponentSheetResponseDto;
import com.danny.ewf_service.payload.response.product.ProductDetailResponseDto;
import com.danny.ewf_service.service.ComponentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/component")
@RestController
@AllArgsConstructor
public class ComponentController {

    @Autowired
    private final ComponentService componentService;

    @GetMapping("/inbound")
    public ResponseEntity<?> getAllComponentsInbound() {
        try {
            List<ComponentInboundResponseDto> componentInboundResponseDtos = componentService.findComponentsInbound();
            return ResponseEntity.ok().body(componentInboundResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching location");
        }
    }


    @GetMapping("/wms")
    public ResponseEntity<?> getAllComponentsWMS(){
        try {
            List<ComponentListWMSResponse> componentListWMSResponses = componentService.findAllComponentsWMS();
            return ResponseEntity.ok().body(componentListWMSResponses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching components");
        }
    }


    @GetMapping("")
    public ResponseEntity<?> getComponents(){
        try {
            List<ComponentSheetResponseDto> componentSheetResponseDtos = componentService.findAllComponentsSheet();
            return ResponseEntity.ok().body(componentSheetResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching components");
        }
    }

    @PostMapping("")
    public ResponseEntity<?> updateComponentsfromSheet(@RequestBody List<ComponentSheetResponseDto> componentSheetResponseDtos) {
        try {
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
