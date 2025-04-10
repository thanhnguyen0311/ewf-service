package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IRoleMapper;
import com.danny.ewf_service.entity.auth.Role;
import com.danny.ewf_service.payload.response.RoleResponseDto;
import com.danny.ewf_service.repository.RoleRepository;
import com.danny.ewf_service.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final IRoleMapper roleMapper;

    @Override
    public List<RoleResponseDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.roleToRoleResponseDtos(roles);
    }
}
