package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IUserMapper;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.request.UserListRequestDto;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;
import com.danny.ewf_service.repository.UserRepository;
import com.danny.ewf_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final IUserMapper iUserMapper;

    @Override
    public List<UserListResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return iUserMapper.usersToUserListResponseDtos(users);
    }

    @Override
    public void updateUser(UserListRequestDto userListRequestDto) {

    }
}
