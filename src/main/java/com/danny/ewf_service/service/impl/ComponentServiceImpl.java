package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IComponentMapper;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.entity.ProductComponent;
import com.danny.ewf_service.payload.response.ComponentResponseDto;
import com.danny.ewf_service.service.ComponentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ComponentServiceImpl implements ComponentService {
    @Autowired
    private final IComponentMapper componentMapper;

    @Override
    public List<ComponentResponseDto> findComponents(Product product) {
        List<ComponentResponseDto> componentResponseDtos = new ArrayList<>();
        List<ProductComponent> productComponents = product.getProductComponents();
        if (productComponents != null) {
            for (ProductComponent productComponent : productComponents) {
                componentResponseDtos.add(componentMapper.componentToComponentResponseDto(
                        productComponent.getComponent()));
            }
        }
        return componentResponseDtos;
    }
}
