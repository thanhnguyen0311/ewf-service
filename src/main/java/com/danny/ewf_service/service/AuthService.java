package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.user.RegisterRequest;
import com.danny.ewf_service.payload.response.user.UserResponseDto;

public interface AuthService {

    void register(RegisterRequest request);

    UserResponseDto getInfo();
}
