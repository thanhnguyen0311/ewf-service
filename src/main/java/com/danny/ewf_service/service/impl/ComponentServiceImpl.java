package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IComponentMapper;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductComponent;
import com.danny.ewf_service.entity.Report;
import com.danny.ewf_service.payload.response.component.ComponentInboundResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentListWMSResponse;
import com.danny.ewf_service.payload.response.component.ComponentResponseDto;
import com.danny.ewf_service.payload.response.component.ComponentSheetResponseDto;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ReportRepository;
import com.danny.ewf_service.service.ComponentService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    @Override
    public List<ComponentSheetResponseDto> findAllComponentsSheet() {
        List<Component> components = componentRepository.findAll();
        return new ArrayList<>(componentMapper.componentListToComponentSheetResponseDtoList(components));
    }

    @Override
    public LocalDateTime updateComponentsFromSheet(List<ComponentSheetResponseDto> componentSheetResponseDtos) {
        if (componentSheetResponseDtos == null || componentSheetResponseDtos.isEmpty()) {
            // Skip processing if the input list is empty
            return LocalDateTime.now();
        }
        // Extract all SKUs from the DTOs
        List<String> skus = componentSheetResponseDtos.stream()
                .map(ComponentSheetResponseDto::getSku)
                .filter(sku -> sku != null && !sku.isEmpty())
                .toList();

        // Fetch existing components by SKUs in bulk
        List<Component> existingComponents = componentRepository.findComponentsBySkuIn(skus);

        // Build a map of existing components for fast lookup
        Map<String, Component> componentMap = existingComponents.stream()
                .collect(Collectors.toMap(Component::getSku, component -> component));

        // Create or update components
        List<Component> componentsToSave = new ArrayList<>();
        for (ComponentSheetResponseDto dto : componentSheetResponseDtos) {
            // Check if the component already exists
            Component component = componentMap.get(dto.getSku());

            if (component == null) {
                // If no existing component, create a new one
                component = new Component();
                component.setSku(dto.getSku());
                component.setCreatedAt(LocalDateTime.now());
            }

            // Update component fields from DTO
            component.setUpc(dto.getUpc());
            component.setManufacturer(dto.getManufacturer());
            component.setType(dto.getType());
            component.setFinish(dto.getFinish());
            component.setCategory(dto.getCategory());
            component.setName(dto.getName());
            component.setFabricColor(dto.getFabricColor());
            component.setFabricCode(dto.getFabricCode());
            component.setSizeShape(dto.getSizeShape());
            component.setCollection(dto.getCollection());
            component.setStyle(dto.getStyle());

            // Add to the save list
            componentsToSave.add(component);
        }

        // Step 5: Save all components in bulk
        componentRepository.saveAll(componentsToSave);

        // Return the timestamp of the operation
        return LocalDateTime.now();
    }
}
