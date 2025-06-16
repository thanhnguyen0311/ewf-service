package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import com.danny.ewf_service.service.BayLocationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/location")
@RestController
@AllArgsConstructor
public class BayLocationController {

    private final BayLocationService bayLocationService;

    @GetMapping("")
    public ResponseEntity<?> getAllBayLocations() {
        try {
            List<BayLocationResponseDto> bayLocationResponseDtos = bayLocationService.findAll();
            return ResponseEntity.ok().body(bayLocationResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching location");
        }
    }


}
