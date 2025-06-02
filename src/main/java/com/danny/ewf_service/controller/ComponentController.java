package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import com.danny.ewf_service.payload.response.ComponentInboundResponseDto;
import com.danny.ewf_service.service.ComponentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/component")
@RestController
@AllArgsConstructor
public class ComponentController {

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
}
