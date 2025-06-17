package com.danny.ewf_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class ValidationException extends RuntimeException {
    private final String field;
    private final String message;
}