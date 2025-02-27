package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IProductMapper;
import com.danny.ewf_service.entity.Product;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;
import com.danny.ewf_service.repository.ProductComponentRepository;
import com.danny.ewf_service.repository.ProductRepository;
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

    @Override
    public PagingResponse<ProductInventoryResponseDto> inventoryProductList(int page) {
        Pageable pageable = PageRequest.of(page, 30);
        Page<Object[]> result = productComponentRepository.calculateProductInventory(pageable);
        List<Object[]> content = result.getContent();
        List<Long> productIds = new ArrayList<>();

        for (Object[] row : content) {
            productIds.add(parseObjectToLong(row[0]));
        }

        List<Product> products = productRepository.findAllByIdIn(productIds);
        List<ProductInventoryResponseDto> productInventoryResponseDtos =
                productMapper.productListToProductInventoryResponseDtoList(products);

        Map<Long, Long> productInventoryMap = result.getContent().stream()
                .collect(Collectors.toMap(
                        row -> parseObjectToLong(row[0]),  // Key: productId
                        row -> parseObjectToLong(row[1])   // Value: inventory
                ));

        productInventoryResponseDtos.forEach(dto -> {
            dto.setQuantity(productInventoryMap.get(dto.getId()));

        });

        return new PagingResponse<>(
                productInventoryResponseDtos,
                result.getNumber(),                                        // Current Page
                result.getTotalPages(),                                    // Total Pages
                result.getSize(),                                          // Page Size
                result.getTotalElements()                                  // Total Elements
        );
    }

    private Long parseObjectToLong(Object object){
        return Long.parseLong(String.valueOf(object));
    }
}
