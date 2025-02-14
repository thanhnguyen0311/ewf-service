package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product findBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

}
