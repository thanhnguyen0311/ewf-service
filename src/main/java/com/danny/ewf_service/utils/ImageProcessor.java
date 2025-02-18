package com.danny.ewf_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageProcessor {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    @Setter
    @Data
    public static class ImageUrls {
        private List<String> dim;
        private List<String> img;
    }

    public ImageUrls parseImageJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, ImageUrls.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing image JSON", e);
        }
    }
}
