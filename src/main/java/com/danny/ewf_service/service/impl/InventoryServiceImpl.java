package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IComponentMapper;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.product.Product;
import com.danny.ewf_service.entity.product.ProductWithQuantity;
import com.danny.ewf_service.payload.request.ComponentInventoryRequestDto;
import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.product.ProductInventoryResponseDto;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.ConfigurationRepository;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.service.ComponentService;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.auth.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final IComponentMapper componentMapper;

    @Autowired
    private final ConfigurationRepository configurationRepository;

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private final ComponentService componentService;

    @Override
    public List<ProductInventoryResponseDto> inventoryProductListByQuantityASC() {
        List<Object[]> rawResult = productComponentRepository.calculateAllProductInventoryByQuantityASC();
        List<ProductWithQuantity> productWithQuantities = rawResult.stream()
                .map(result -> new ProductWithQuantity(
                        (Product) result[0],          // The entire Product entity
                        ((Number) result[1]).longValue() // The calculated inventory (quantity)
                ))
                .toList();

        return new ArrayList<>();
    }

//    @Override
//    public PagingResponse<ProductInventoryResponseDto> inventoryProductSearchBySku(int page, String sku) {
//        Pageable pageable = PageRequest.of(page, 30);
//        Page<Object[]> result = productInventorySearching.productInventorySearchBySku(pageable, sku);
//        return inventoryProductResponse(result);
//    }

    @Override
    public List<ProductInventoryResponseDto> inventoryProductAll() {
        List<Object[]> rawResults = productComponentRepository.productInventoryAll();

        return rawResults.parallelStream() // Use parallel processing
                .map(row -> new ProductInventoryResponseDto(
                        ((Number) row[0]).longValue(),  // id
                        (String) row[1],                // sku
                        ((Number) row[2]).longValue(),  // quantity
                        (String) row[3],                // asin
                        (String) row[4],                // upc
                        (Boolean) row[5],               // discontinued
                        (Boolean) row[6],               // ewfdirect
                        (Boolean) row[7],               // amazon
                        (Boolean) row[8],               // cymax
                        (Boolean) row[9],               // overstock
                        (Boolean) row[10],              // wayfair
                        (String) row[11])).toList();    // localSku
    }

    @Override
    public Long getInventoryProductCountById(Long id) {
        Long productInventory = productComponentRepository.countByProductId(id);
        if (productInventory == null) {
            productInventory = 0L;
        }
        return productInventory;
    }

    @Override
    public List<ComponentInventoryResponseDto> findAllComponentsInventory() {
        List<Component> components = componentService.findAllComponents();
        double toBeShippedRate = Double.parseDouble(configurationRepository.findByName("to_be_shippped_rate").getValue());
        List<ComponentInventoryResponseDto> componentInventoryResponseDtos = componentMapper.componentListToComponentInventoryResponseDtoList(components);
        for (ComponentInventoryResponseDto componentInventoryResponseDto : componentInventoryResponseDtos) {
            componentInventoryResponseDto.setToBeShipped(calculateToBeShipped(componentInventoryResponseDto, toBeShippedRate));
        }
        return componentInventoryResponseDtos;
    }

    @Override
    public ComponentInventoryResponseDto updateComponent(ComponentInventoryRequestDto componentInventoryRequestDto) {
        Component component = componentService.findComponentById(componentInventoryRequestDto.getId());
        component.setName(componentInventoryRequestDto.getName());
        component.setInventory(componentInventoryRequestDto.getInventory());
        component.setDiscontinue(componentInventoryRequestDto.getDiscontinue());
        component.getReport().setInProduction(componentInventoryRequestDto.getInProduction());
        component.getReport().setOnPO(componentInventoryRequestDto.getOnPO());
        component.getReport().setToShip(componentInventoryRequestDto.getToShip());
        component.getReport().setInTransit(componentInventoryRequestDto.getInTransit());
        component.getReport().setStockVN(componentInventoryRequestDto.getStockVN());
        componentRepository.save(component);
        double toBeShippedRate = Double.parseDouble(configurationRepository.findByName("to_be_shippped_rate").getValue());
        ComponentInventoryResponseDto componentInventoryResponseDto = componentMapper.componentToComponentInventoryResponseDto(component);
        componentInventoryResponseDto.setToBeShipped(calculateToBeShipped(componentInventoryResponseDto, toBeShippedRate));
        return componentInventoryResponseDto;
    }

    private Long parseObjectToLong(Object object) {
        return Long.parseLong(String.valueOf(object));
    }

//    private PagingResponse<ProductInventoryResponseDto> inventoryProductResponse(Page<Object[]> pageObject){
//        List<Object[]> content = pageObject.getContent();
//        List<Long> productIds = new ArrayList<>();
//
//        for (Object[] row : content) {
//            productIds.add(parseObjectToLong(row[0]));
//        }
//
//        List<Product> products = productRepository.findAllByIdIn(productIds);
//        List<ProductInventoryResponseDto> productInventoryResponseDtos =
//                productMapper.productListToProductInventoryResponseDtoList(products);
//
//        Map<Long, Long> productInventoryMap = pageObject.getContent().stream()
//                .collect(Collectors.toMap(
//                        row -> parseObjectToLong(row[0]),  // Key: productId
//                        row -> parseObjectToLong(row[1])   // Value: inventory
//                ));
//
//        productInventoryResponseDtos.forEach(dto -> {
//            dto.setQuantity(productInventoryMap.get(dto.getId()));
//        });
//
//        return new PagingResponse<>(
//                productInventoryResponseDtos,
//                pageObject.getNumber(),                                        // Current Page
//                pageObject.getTotalPages(),                                    // Total Pages
//                pageObject.getSize(),                                          // Page Size
//                pageObject.getTotalElements()                                  // Total Elements
//        );
//    }

    private String calculateToBeShipped(ComponentInventoryResponseDto componentInventoryResponseDto, double toBeShippedRate) {
        double calculatedValue = toBeShippedRate * componentInventoryResponseDto.getReport120Days()
                                 - (componentInventoryResponseDto.getInStock()
                                    + componentInventoryResponseDto.getToShip()
                                    + componentInventoryResponseDto.getInTransit());
        double additionalNeeded = Math.min(calculatedValue, componentInventoryResponseDto.getInProduction() + componentInventoryResponseDto.getStockVN());
        long result = Math.round(additionalNeeded);
        if (additionalNeeded < 0) {
            return "Dư " + (-result);
        } else {
            return "Thiếu " + result;
        }
    }
}
