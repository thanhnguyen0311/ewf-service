package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.request.product.ProductImageRequestDto;

import java.util.List;

public interface ImageService {

    List<String> getAllProductImages(Product product);

    String buildJsonString(ImageUrls imageUrl);

    ImageUrls parseImageJson(String jsonString);

    List<String> toList(ImageUrls imageUrl);

    ImageUrls updateProductImages(ProductImageRequestDto productImageRequestDto);
}
