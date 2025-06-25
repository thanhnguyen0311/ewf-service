package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.entity.LooseInventory;
import com.danny.ewf_service.payload.request.ComponentInventoryRequestDto;
import com.danny.ewf_service.payload.response.component.ComponentInventoryResponseDto;
import com.danny.ewf_service.payload.response.product.ProductInventoryResponseDto;

import java.util.List;


public interface InventoryService {

    List<ProductInventoryResponseDto> inventoryProductListByQuantityASC();

    List<ProductInventoryResponseDto> inventoryProductAll();

    Long getInventoryProductCountById(Long id);

    List<ComponentInventoryResponseDto> findAllComponentsInventory();

    ComponentInventoryResponseDto updateComponent(ComponentInventoryRequestDto componentInventoryRequestDto);

    Long getLooseInventoryByTagID(String tagID);

    LooseInventory findLooseInventoryByLpn(LPN lpn);
}
