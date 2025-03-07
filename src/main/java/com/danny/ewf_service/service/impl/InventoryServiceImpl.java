package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
import com.danny.ewf_service.repository.inventory.ProductInventorySearching;
import com.danny.ewf_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.danny.ewf_service.payload.response.PagingResponse;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private final ProductComponentRepository productComponentRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final IProductMapper productMapper;

    @Autowired
    private final ProductInventorySearching productInventorySearching;

    @Override
    public PagingResponse<ProductInventoryResponseDto> inventoryProductListByQuantityASC(int page) {
        Pageable pageable = PageRequest.of(page, 30);
        Page<Object[]> result = productComponentRepository.calculateProductInventoryByQuantityASC(pageable);
        return inventoryProductResponse(result);
    }

    @Override
    public PagingResponse<ProductInventoryResponseDto> inventoryProductSearchBySku(int page, String sku) {
        Pageable pageable = PageRequest.of(page, 30);
        Page<Object[]> result = productInventorySearching.productInventorySearchBySku(pageable, sku);
        return inventoryProductResponse(result);
    }

    private Long parseObjectToLong(Object object){
        return Long.parseLong(String.valueOf(object));
    }

    private PagingResponse<ProductInventoryResponseDto> inventoryProductResponse(Page<Object[]> pageObject){
        List<Object[]> content = pageObject.getContent();
        List<Long> productIds = new ArrayList<>();

        for (Object[] row : content) {
            productIds.add(parseObjectToLong(row[0]));
        }

        List<Product> products = productRepository.findAllByIdIn(productIds);
        List<ProductInventoryResponseDto> productInventoryResponseDtos =
                productMapper.productListToProductInventoryResponseDtoList(products);

        Map<Long, Long> productInventoryMap = pageObject.getContent().stream()
                .collect(Collectors.toMap(
                        row -> parseObjectToLong(row[0]),  // Key: productId
                        row -> parseObjectToLong(row[1])   // Value: inventory
                ));

        productInventoryResponseDtos.forEach(dto -> {
            dto.setQuantity(productInventoryMap.get(dto.getId()));

        });

        return new PagingResponse<>(
                productInventoryResponseDtos,
                pageObject.getNumber(),                                        // Current Page
                pageObject.getTotalPages(),                                    // Total Pages
                pageObject.getSize(),                                          // Page Size
                pageObject.getTotalElements()                                  // Total Elements
        );
    }
}
