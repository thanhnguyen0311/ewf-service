package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.RoleResponseDto;

import java.util.List;

public interface RoleService {
    List<RoleResponseDto> findAll();
}
