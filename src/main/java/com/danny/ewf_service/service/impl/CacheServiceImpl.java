package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.service.CacheService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
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
    @Cacheable(value = "productsCache", key = "'allProducts'")
    public List<Product> getAllProducts() {
        System.out.println("Cache miss - loading all products from database");
        List<Product> products;
        try {
            products = productRepository.findAllProducts();
        } catch (Exception e) {
            System.err.println("Error while fetching all products: " + e.getMessage());
            throw new RuntimeException("Failed to fetch products from the database", e);
        }
        return products;
    }



    @Override
    @Cacheable(value = "productsCache", key = "#productId")
    public Product getProductById(Long productId) {
        System.out.println("Cache miss - loading product " + productId + " from database");
        return productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Override
    @Transactional
    @CachePut(value = "productsCache", key = "#product.id")
    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        // Update product in the allProducts cache list if it exists
        updateProductInAllProductsCache(savedProduct);

        return savedProduct;
    }

    private void updateProductInAllProductsCache(Product updatedProduct) {
        Cache cache = cacheManager.getCache("productsCache");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get("allProducts");
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) wrapper.get();
                if (products != null) {
                    // Find and replace the updated product in the list
                    boolean found = false;
                    for (int i = 0; i < products.size(); i++) {
                        if (products.get(i).getId().equals(updatedProduct.getId())) {
                            products.set(i, updatedProduct);
                            found = true;
                            break;
                        }
                    }

                    // If product wasn't in the list, add it (for new products)
                    if (!found) {
                        products.add(updatedProduct);
                    }

                    // Update the cache with the modified list
                    cache.put("allProducts", products);
                }
            }
        }
    }
}