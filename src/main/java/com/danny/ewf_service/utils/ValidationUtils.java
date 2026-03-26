package com.danny.ewf_service.utils;

import org.springframework.stereotype.Service;

@Service
public class ValidationUtils {
    public boolean isValidNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
