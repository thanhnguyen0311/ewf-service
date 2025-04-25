package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.request.product.ProductImageRequestDto;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import com.danny.ewf_service.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CacheService cacheService;


    @Override
    public List<String> getAllProductImages(Product product) {
        return List.of();
    }

    @Override
    public String buildJsonString(ImageUrls imageUrl) {
        try {
            // Handle null lists to avoid NullPointerException
            List<String> img = imageUrl.getImg() != null ? imageUrl.getImg() : new ArrayList<>();
            List<String> dim = imageUrl.getDim() != null ? imageUrl.getDim() : new ArrayList<>();
            List<String> cgi = imageUrl.getCgi() != null ? imageUrl.getCgi() : new ArrayList<>();

            // Sorting logic
            img.sort(Comparator.comparing((String link) -> !link.contains("/CGI/"))
                    .thenComparing(link -> !link.contains("/DNS/")));
            dim.sort(String::compareTo);

            // Use a map to store the JSON structure
            Map<String, List<String>> imagesMap = new HashMap<>();
            imagesMap.put("dim", dim);
            imagesMap.put("img", img);
            imagesMap.put("cgi", cgi);

            return OBJECT_MAPPER.writeValueAsString(imagesMap);
        } catch (Exception e) {
            // In case of errors, return a default empty JSON
            return "{\"img\":[],\"dim\":[],\"cgi\":[]}";
        }
    }

    @Override
    public ImageUrls parseImageJson(String jsonString) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, ImageUrls.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON for ImageUrls: " + jsonString, e);
        }
    }

    @Override
    public List<String> toList(ImageUrls imageUrl) {
        List<String> images = new ArrayList<>();
        images.addAll(imageUrl.getCgi());
        images.addAll(imageUrl.getImg());
        images.addAll(imageUrl.getDim());
        return images;
    }

    @Override
    public ImageUrls updateProductImages(ProductImageRequestDto productImageRequestDto) {
        Product product = productRepository.findById(productImageRequestDto.getId()).orElseThrow(() -> new RuntimeException("Product not found with id: " + productImageRequestDto.getId()));
        ImageUrls newImage = productImageRequestDto.getImages();
        if (newImage != null) {
            product.setImages(buildJsonString(productImageRequestDto.getImages()));
            cacheService.saveProduct(product);
        }
        return newImage;
    }

}
