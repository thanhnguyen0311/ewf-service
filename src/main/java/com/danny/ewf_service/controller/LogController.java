package com.danny.ewf_service.controller;

import com.danny.ewf_service.entity.LpnLog;
import com.danny.ewf_service.exception.ResourceNotFoundException;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.payload.response.log.LpnLogResponseDto;
import com.danny.ewf_service.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/history")
@RestController
@AllArgsConstructor
public class LogController {

    @Autowired
    private final LogService logService;

    @GetMapping("/lpn")
    public ResponseEntity<?> getAllLpn(@RequestParam(defaultValue = "0") int page) {
        try {
            List<LpnLogResponseDto> logs = logService.findAllLpnLogs(page);

            if (logs.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to retrieve LPN logs: " + e.getMessage());

        }
    }
}
