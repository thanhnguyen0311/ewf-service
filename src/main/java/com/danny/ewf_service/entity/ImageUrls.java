package com.danny.ewf_service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ImageUrls {
    private List<String> cgi = new ArrayList<>();
    private List<String> dim = new ArrayList<>();
    private List<String> img = new ArrayList<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ImageUrls parseImageJson(String jsonString) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, ImageUrls.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON for ImageUrls: " + jsonString, e);
        }
    }
}
