package com.danny.ewf_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForbiddenException extends RuntimeException {
    private final String requiredRole;

}