package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;

import java.util.List;

public interface InventoryService {

    List<ProductInventoryResponseDto> inventoryProductList(int page);
}
