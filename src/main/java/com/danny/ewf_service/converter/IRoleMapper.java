package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.auth.Role;
import com.danny.ewf_service.payload.response.RoleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IRoleMapper {

    @Mapping(target = "id", source = "role.id")
    @Mapping(target = "slug", source = "role.slug")
    RoleResponseDto roleToRoleResponseDto(Role role);
    List<RoleResponseDto> roleToRoleResponseDtos(List<Role> roles);
}
