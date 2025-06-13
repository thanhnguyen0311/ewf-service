package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IUserMapper;
import com.danny.ewf_service.entity.auth.Role;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.request.user.RegisterRequest;
import com.danny.ewf_service.payload.request.user.UserCreateRequestDto;
import com.danny.ewf_service.payload.request.user.UserListRequestDto;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;
import com.danny.ewf_service.repository.RoleRepository;
import com.danny.ewf_service.repository.UserRepository;
import com.danny.ewf_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final IUserMapper iUserMapper;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserListResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return iUserMapper.usersToUserListResponseDtos(users);
    }

    @Override
    public void updateUser(UserListRequestDto userListRequestDto) {
        System.out.println(userListRequestDto);
        Optional<User> optionalUser = userRepository.findById(userListRequestDto.getId());
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setFirstName(userListRequestDto.getFirstName());
            user.setLastName(userListRequestDto.getLastName());
            user.setIsActive(userListRequestDto.getIsActive());
            Optional<Role> optionalRole = roleRepository.findById(userListRequestDto.getRoleId());
            if (optionalRole.isPresent()){
                Role role = optionalRole.get();
                user.setRole(role);
            }
            userRepository.save(user);
        }
    }

    @Override
    public void createUser(UserCreateRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken!");
        }

        Role role = roleRepository.findBySlug(request.getRoleSlug())
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(request.getStatus())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }
}
