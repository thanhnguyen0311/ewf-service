package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto findBySku(String sku);

    List<ProductResponseDto> findAll();

    List<ProductSearchResponseDto> getAllProductsSearch();

    void saveProduct(Product product);

    List<Product> findMergedProducts(Product product);
}
