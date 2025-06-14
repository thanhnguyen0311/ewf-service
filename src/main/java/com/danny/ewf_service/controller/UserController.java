package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.user.UserCreateRequestDto;
import com.danny.ewf_service.payload.request.user.UserListRequestDto;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;
import com.danny.ewf_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/users")
@RestController
@AllArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUsers() {
        try {
            List<UserListResponseDto> userListResponseDtoList = userService.findAll();
            return ResponseEntity.ok(userListResponseDtoList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Users not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @PutMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@RequestBody UserListRequestDto userListRequestDto) {
        try {
            userService.updateUser(userListRequestDto);
            return ResponseEntity.status(200).body("User updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Users not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequestDto userCreateRequestDto) {
        try {
            userService.createUser(userCreateRequestDto);
            return ResponseEntity.status(200).body("User created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }
}
