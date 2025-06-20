package com.danny.ewf_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle ValidationException
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("field", ex.getField());
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "unauthorized");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("code", "NOT_AUTHENTICATED");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle ForbiddenException (when user doesn't have required role)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbiddenException(ForbiddenException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "forbidden");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("requiredRole", ex.getRequiredRole());
        errorResponse.put("code", "INSUFFICIENT_PERMISSIONS");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}