package com.danny.ewf_service.service;


import com.danny.ewf_service.entity.Product;

public interface ProductService {

    Product findBySku(String sku);
}
