package com.danny.ewf_service.converter;


import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.response.user.UserListResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IUserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role.slug")
    @Mapping(target = "roleId", source = "user.role.id")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "lastLogin", source = "user.lastLogin")
    UserListResponseDto userToUserListResponseDto(User user);
    List<UserListResponseDto> usersToUserListResponseDtos(List<User> users);
}
