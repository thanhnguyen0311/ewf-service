package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IComponentMapper;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.Report;
import com.danny.ewf_service.payload.response.component.ComponentInboundResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentListWMSResponse;
import com.danny.ewf_service.payload.response.component.ComponentResponseDto;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ReportRepository;
import com.danny.ewf_service.service.ComponentService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ComponentServiceImpl implements ComponentService {
    @Autowired
    private final IComponentMapper componentMapper;

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private final ReportRepository reportRepository;

    @Override
    public List<ComponentResponseDto> findComponents(Product product) {
        List<ComponentResponseDto> componentResponseDtos = new ArrayList<>();
        List<ProductComponent> productComponents = product.getComponents();
        if (productComponents != null) {
            for (ProductComponent productComponent : productComponents) {
                if (Objects.equals(productComponent.getComponent().getSku(), product.getSku())) continue;
                componentResponseDtos.add(componentMapper.componentToComponentResponseDto(
                        productComponent.getComponent()));
            }
        }
        return componentResponseDtos;
    }

    @Override
    public List<Component> findAllComponents() {
        return componentRepository.findAllComponents();
    }

    @Override
    @Transactional
    public void linkComponentsToReports() {
        List<Component> components = componentRepository.findAllComponents();
        for (Component component : components) {
            Report report = new Report();
            report = reportRepository.save(report);
            component.setReport(report);
            System.out.println(component.getSku());
            componentRepository.save(component);
        }
    }

    @Override
    public List<ComponentInboundResponseDto> findComponentsInbound() {
        List<Component> components = componentRepository.findAllComponents();
        return componentMapper.componentListToComponentInboundResponseDtoList(components).stream()
                .peek(component -> {
                    if (component.getUpc() == null) {
                        component.setUpc("");
                    }
                })
                .sorted(Comparator.comparing(ComponentInboundResponseDto::getSku))
                .collect(Collectors.toList());

    }

    @Override
    public Component findComponentById(Long id) {
        return componentRepository.findComponentById(id);
    }

    @Override
    public List<ComponentListWMSResponse> findAllComponentsWMS() {
        List<Component> components = componentRepository.findAllByDiscontinueFalse();
        return componentMapper.componentListToComponentListWMSResponseDtoList(components);
    }
}
