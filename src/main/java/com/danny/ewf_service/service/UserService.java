package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.UserListRequestDto;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;

import java.util.List;

public interface UserService {

    List<UserListResponseDto> findAll();

    void updateUser(UserListRequestDto userListRequestDto);
}
