package com.danny.ewf_service.payload.request.user;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class UserCreateRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private Boolean status;
    private String password;
    private String roleSlug;
}
