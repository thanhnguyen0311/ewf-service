package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.ShopifyOrder;
import com.danny.ewf_service.payload.request.sheet.ShopifyOrderRequestDto;

import java.util.List;

public interface ShopifyOrderService {

    void updateShopifyOrder(ShopifyOrderRequestDto shopifyOrder);

    List<ShopifyOrder> getAllShopifyOrders();


}
