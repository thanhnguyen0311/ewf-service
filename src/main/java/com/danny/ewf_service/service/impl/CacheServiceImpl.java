package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CacheServiceImpl implements CacheService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CacheManager cacheManager;

    @Override
    @Cacheable(value = "productsCache")
    public List<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    public void reloadProductInCache(Product product) {
        Objects.requireNonNull(cacheManager.getCache("productsCache")).put(product.getId(), product);
    }

}
