package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.response.RoleResponseDto;
import com.danny.ewf_service.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/roles")
@RestController
@AllArgsConstructor
public class RoleController {

    @Autowired
    private final RoleService roleService;

    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getRoles() {
        try {
            List<RoleResponseDto> roleResponseDtos = roleService.findAll();
            return ResponseEntity.ok(roleResponseDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Users not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

}