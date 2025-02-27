package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.PagingResponse;
import com.danny.ewf_service.payload.response.ProductInventoryResponseDto;


public interface InventoryService {

    PagingResponse<ProductInventoryResponseDto> inventoryProductList(int page);
}
