package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.service.LpnService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/lpn")
@RestController
@AllArgsConstructor
public class LPNController {

    private final LpnService lpnService;

    @PostMapping("/new")
    public ResponseEntity<?> createNewLpn(@RequestBody LpnRequestDto lpn) {
        try {
            lpnService.newLpn(lpn);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @GetMapping("")
    public  ResponseEntity<?> getAllLpn() {
        try {
            List<LpnResponseDto> lpnResponseDtos = lpnService.getAllLpn();
            return ResponseEntity.ok(lpnResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
}
