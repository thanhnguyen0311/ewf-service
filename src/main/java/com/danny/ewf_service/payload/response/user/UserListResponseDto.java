package com.danny.ewf_service.payload.response.user;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class UserListResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Long roleId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean isActive;
}
