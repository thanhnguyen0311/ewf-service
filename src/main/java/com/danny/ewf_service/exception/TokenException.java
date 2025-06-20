package com.danny.ewf_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenException extends RuntimeException {
    private final String code;
}
