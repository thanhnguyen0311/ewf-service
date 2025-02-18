package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.ProductResponseDto;

public interface ProductService {

    ProductResponseDto findBySku(String sku);
}
