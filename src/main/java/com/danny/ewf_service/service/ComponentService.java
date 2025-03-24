package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.response.ComponentResponseDto;

import java.util.List;

public interface ComponentService {

    List<ComponentResponseDto>  findComponents (Product product);

    List<Component> findAllComponents();

    void linkComponentsToReports();

    Component findComponentById(Long id);
}
