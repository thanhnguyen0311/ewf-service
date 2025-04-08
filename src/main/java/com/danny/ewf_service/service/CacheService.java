package com.danny.ewf_service.service;


import com.danny.ewf_service.entity.product.Product;

import java.util.List;

public interface CacheService {

    List<Product> getAllProducts();


    Product getProductById(Long id);

    Product saveProduct(Product product);

}

