package com.danny.ewf_service.service.auth;

import com.danny.ewf_service.entity.auth.Role;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.request.user.RegisterRequest;
import com.danny.ewf_service.payload.response.user.UserResponseDto;
import com.danny.ewf_service.repository.RoleRepository;
import com.danny.ewf_service.repository.UserRepository;
import com.danny.ewf_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
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

        Role role = roleRepository.findBySlug("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(false) // Set true if the user is active by default
                .createdAt(LocalDateTime.now()) // Set registration date
                .build();

        userRepository.save(user);
    }

    @Override
    public UserResponseDto getInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserResponseDto userResponseDto = new UserResponseDto();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userResponseDto.setEmail(userDetails.getUsername());
            userResponseDto.setFirstName(userDetails.getFirstName());
            userResponseDto.setLastName(userDetails.getLastName());
            userResponseDto.setRole(userDetails.getRole());
            return userResponseDto;
        }
        return null;
    }
}
