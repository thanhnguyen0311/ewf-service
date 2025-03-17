package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.ComponentInventoryRequestDto;
import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;

import java.util.List;


public interface InventoryService {

    List<ProductInventoryResponseDto> inventoryProductListByQuantityASC();

    PagingResponse<ProductInventoryResponseDto> inventoryProductSearchBySku(int page, String sku);

    Long getInventoryProductCountById(Long id);

    List<ComponentInventoryResponseDto> findAllComponentsInventory();

    ComponentInventoryResponseDto updateComponent(ComponentInventoryRequestDto componentInventoryRequestDto);
}
