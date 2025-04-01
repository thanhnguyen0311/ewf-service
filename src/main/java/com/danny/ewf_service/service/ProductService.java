package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.response.ProductDetailResponseDto;
import com.danny.ewf_service.payload.response.ProductResponseDto;
import com.danny.ewf_service.payload.response.ProductSearchResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto findBySku(String sku);

    List<ProductDetailResponseDto> findAllProductsToDtos();

    Product getProductById(Long id);

    List<ProductSearchResponseDto> getAllProductsSearch();

    Product saveProduct(Product product);

    List<Product> findMergedProducts(Product product);
}
