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

    @PutMapping("/putaway")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN')")
    public ResponseEntity<?> putAwayLpn(@RequestBody LpnEditRequestDto lpn) {
        lpnService.putAway(lpn);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/breakdown")
    @PreAuthorize("hasAnyAuthority('ROLE_WORKER', 'ROLE_ADMIN')")
    public ResponseEntity<?> breakDownLpn(@RequestBody LpnEditRequestDto lpn) {
        lpnService.breakDown(lpn);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteLpn(@RequestParam String tagID) {
        lpnService.delete(tagID);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<?> getAllLpn() {
        List<LpnResponseDto> lpnResponseDtos = lpnService.getAllLpn();
        return ResponseEntity.ok(lpnResponseDtos);
    }

    @GetMapping("/tag")
    public ResponseEntity<?> getLpnByTagId(@RequestParam String tagID) {
        LpnResponseDto lpnResponseDto = lpnService.getLpnById(tagID);
        if (lpnResponseDto != null) {
            return ResponseEntity.ok(lpnResponseDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
