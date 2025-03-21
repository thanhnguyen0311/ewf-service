package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.product.LocalProduct;
import com.danny.ewf_service.repository.LocalRepository;
import com.danny.ewf_service.service.LocalService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LocalServiceImpl implements LocalService {
    private final LocalRepository localRepository;


    @Override
    public List<LocalProduct> getAllLocalProducts() {
        return localRepository.findAllWithProducts();
    }

}
