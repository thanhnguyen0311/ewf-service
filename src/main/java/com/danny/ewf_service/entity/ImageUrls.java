package com.danny.ewf_service.entity;

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
    private List<String> dim = new ArrayList<>();
    private List<String> img = new ArrayList<>();



    public String buildJsonString() {
        Map<String, List<String>> imagesMap = new HashMap<>();
        img.sort(Comparator.comparing((String link) -> !link.contains("/CGI/"))
                .thenComparing(link -> !link.contains("/DNS/")));
        imagesMap.put("dim", dim);
        imagesMap.put("img", img);
        StringBuilder jsonBuilder = new StringBuilder("{");

        // Add 'img' links
        jsonBuilder.append("\"img\": [");
        for (int i = 0; i < imagesMap.get("img").size(); i++) {
            jsonBuilder.append("\"").append(imagesMap.get("img").get(i)).append("\"");
            if (i < imagesMap.get("img").size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("],");

        // Add 'dim' links
        jsonBuilder.append("\"dim\": [");
        for (int i = 0; i < imagesMap.get("dim").size(); i++) {
            jsonBuilder.append("\"").append(imagesMap.get("dim").get(i)).append("\"");
            if (i < imagesMap.get("dim").size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }
}
