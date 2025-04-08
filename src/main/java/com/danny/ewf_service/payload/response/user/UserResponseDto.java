package com.danny.ewf_service.payload.response.user;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
