package com.danny.ewf_service.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtility.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                        if (jwtUtility.validateToken(jwt, userDetails.getUsername())) {
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        } else {
                            handleInvalidToken(response, "Token validation failed", "INVALID_TOKEN");
                            return;
                        }
                    } catch (UsernameNotFoundException e){
                        handleInvalidToken(response, "User not found", "USER_NOT_FOUND");
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                handleInvalidToken(response, "Token has expired", "TOKEN_EXPIRED");
                return;
            } catch (MalformedJwtException e) {
                handleInvalidToken(response, "Malformed JWT token", "MALFORMED_TOKEN");
                return;
            } catch (UnsupportedJwtException e) {
                handleInvalidToken(response, "Unsupported JWT token", "UNSUPPORTED_TOKEN");
                return;
            } catch (SignatureException e) {
                handleInvalidToken(response, "Invalid JWT signature", "INVALID_SIGNATURE");
                return;
            } catch (JwtException e) {
                handleInvalidToken(response, "Invalid token: " + e.getMessage(), "INVALID_TOKEN");
                return;
            }

        }

        chain.doFilter(request, response);
    }

    private void handleInvalidToken(HttpServletResponse response, String message, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("code", code);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}