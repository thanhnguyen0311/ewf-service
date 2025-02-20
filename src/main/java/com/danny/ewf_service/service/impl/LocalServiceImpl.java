package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.LocalProduct;
import com.danny.ewf_service.repository.LocalRepository;
import com.danny.ewf_service.service.LocalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalServiceImpl implements LocalService {
    private final LocalRepository localRepository;

    public LocalServiceImpl(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public List<LocalProduct> getAllLocalProducts() {
        return localRepository.findAllWithProducts();
    }
}
