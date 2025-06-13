package com.danny.ewf_service.payload.request.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class UserListRequestDto {
    private Long id;
    private String firstName;
    private String lastName;
    private Long roleId;
    private Boolean isActive;
}
