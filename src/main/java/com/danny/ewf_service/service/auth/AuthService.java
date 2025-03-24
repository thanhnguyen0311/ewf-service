package com.danny.ewf_service.service.auth;

import com.danny.ewf_service.entity.auth.Role;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.request.RegisterRequest;
import com.danny.ewf_service.repository.RoleRepository;
import com.danny.ewf_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken!");
        }
        // Assign default ROLE_USER (you can adjust this based on your use case)
        Role role = roleRepository.findBySlug("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        // Create a new user entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true) // Set true if the user is active by default
                .registeredAt(new java.util.Date()) // Set registration date
                .build();

        // Save the user to the database
        userRepository.save(user);
    }
}
