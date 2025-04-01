package com.danny.ewf_service.service.auth;

import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new CustomUserDetails(
                username,
                user.getPasswordHash(),
                user.getActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(String.valueOf(user.getRole().getSlug()))), // Authorities can be added later if needed
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                String.valueOf(user.getRole().getSlug())
        );
    }

    public String getUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getFirstName() + " " + userDetails.getLastName();
        }
        return "";
    }
}