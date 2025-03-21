package com.danny.ewf_service.service;


import com.danny.ewf_service.entity.product.LocalProduct;

import java.util.List;

public interface LocalService {
    List<LocalProduct> getAllLocalProducts();
}