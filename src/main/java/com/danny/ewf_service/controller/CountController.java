package com.danny.ewf_service.controller;


import com.danny.ewf_service.payload.request.CountingBySkuRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.service.CountService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/counting")
@AllArgsConstructor
public class CountController {

    @Autowired
    private final CountService countService;


    @PostMapping("/sku")
    public ResponseEntity<?> getLpnByTagId(@RequestBody CountingBySkuRequestDto countingBySkuRequestDto) {
        List<LpnResponseDto> lpnResponseDtos = countService.findLpnByListTagID(countingBySkuRequestDto);
        return ResponseEntity.ok(lpnResponseDtos);
    }
}
