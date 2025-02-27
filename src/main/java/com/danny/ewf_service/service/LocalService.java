package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.entity.Product;

import java.util.List;

public interface LocalService {
    List<LocalProduct> getAllLocalProducts();
}