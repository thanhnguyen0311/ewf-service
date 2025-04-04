package com.danny.ewf_service.service.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@ToString
public class CustomUserDetails extends User {

    private final Long userId;
    private final String firstName;
    private final String lastName;

    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, Long userId, String firstName, String lastName) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
