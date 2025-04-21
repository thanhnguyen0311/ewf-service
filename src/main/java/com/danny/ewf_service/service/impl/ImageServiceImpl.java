package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

    @Override
    public List<String> getAllProductImages(Product product) {

        return List.of();
    }

    @Override
    public String buildJsonString(ImageUrls imageUrl) {
        Map<String, List<String>> imagesMap = new HashMap<>();

        imageUrl.getImg().sort(Comparator.comparing((String link) -> !link.contains("/CGI/"))
                .thenComparing(link -> !link.contains("/DNS/")));
        imagesMap.put("dim", imageUrl.getDim());
        imagesMap.put("img", imageUrl.getImg());
        imagesMap.put("cgi", imageUrl.getCgi());

        imagesMap.get("dim").sort(String::compareTo);

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
        jsonBuilder.append("],");

        // Add 'cgi' links
        jsonBuilder.append("\"cgi\": [");
        for (int i = 0; i < imagesMap.get("cgi").size(); i++) {
            jsonBuilder.append("\"").append(imagesMap.get("cgi").get(i)).append("\"");
            if (i < imagesMap.get("cgi").size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }
}
