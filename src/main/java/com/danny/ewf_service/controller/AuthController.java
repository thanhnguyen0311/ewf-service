package com.danny.ewf_service.controller;

import com.danny.ewf_service.configuration.security.JwtUtility;
import com.danny.ewf_service.payload.request.RegisterRequest;
import com.danny.ewf_service.service.auth.AuthService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Setter
    @Getter
    @ToString
    public static class AuthRequest {
        private String email;
        private String password;
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<String>
    login(@RequestBody AuthRequest request) throws AuthenticationException {
        try {
            System.out.println(request.toString());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            return ResponseEntity.ok(jwtUtility.generateToken(request.getEmail()));
        }  catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

}

