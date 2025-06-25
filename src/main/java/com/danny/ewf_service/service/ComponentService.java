package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.payload.response.component.ComponentInboundResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentListWMSResponse;
import com.danny.ewf_service.payload.response.component.ComponentResponseDto;

import java.util.List;

public interface ComponentService {

    List<ComponentResponseDto>  findComponents (Product product);

    List<Component> findAllComponents();

    void linkComponentsToReports();

    List<ComponentInboundResponseDto> findComponentsInbound();

    Component findComponentById(Long id);

    List<ComponentListWMSResponse> findAllComponentsWMS();
}
