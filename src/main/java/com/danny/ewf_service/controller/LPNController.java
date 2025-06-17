package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.LpnEditRequestDto;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.service.LpnService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/lpn")
@RestController
@AllArgsConstructor
public class LPNController {

    @Autowired
    private final LpnService lpnService;

    @PostMapping("/new")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createNewLpn(@RequestBody LpnRequestDto lpn) {
        lpnService.newLpn(lpn);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN')")
    public ResponseEntity<?> editLpn(@RequestBody LpnEditRequestDto lpn) {
        lpnService.updateLpn(lpn);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<?> getAllLpn() {
        List<LpnResponseDto> lpnResponseDtos = lpnService.getAllLpn();
        return ResponseEntity.ok(lpnResponseDtos);
    }


}
