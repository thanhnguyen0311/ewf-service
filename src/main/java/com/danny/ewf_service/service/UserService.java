package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.user.RegisterRequest;
import com.danny.ewf_service.payload.request.user.UserCreateRequestDto;
import com.danny.ewf_service.payload.request.user.UserListRequestDto;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;

import java.util.List;

public interface UserService {

    List<UserListResponseDto> findAll();

    void updateUser(UserListRequestDto userListRequestDto);

    void createUser(UserCreateRequestDto userCreateRequestDto);
}
