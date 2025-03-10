package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;

import java.util.List;


public interface InventoryService {

    PagingResponse<ProductInventoryResponseDto> inventoryProductListByQuantityASC(int page);

    PagingResponse<ProductInventoryResponseDto> inventoryProductSearchBySku(int page, String sku);

    Long getInventoryProductCountById(Long id);

    List<ComponentInventoryResponseDto> findAllComponentsInventory();
}
