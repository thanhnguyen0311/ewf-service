package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.ShopifyOrder;
import com.danny.ewf_service.payload.request.sheet.ShopifyOrderRequestDto;
import com.danny.ewf_service.repository.ShopifyOrderRepository;
import com.danny.ewf_service.service.ShopifyOrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ShopifyOrderServiceImpl implements ShopifyOrderService {

    @Autowired
    private final ShopifyOrderRepository shopifyOrderRepository;
    @Override
    public void updateShopifyOrder(ShopifyOrderRequestDto shopifyOrderRequestDto) {
        ShopifyOrder shopifyOrder = shopifyOrderRepository.findByOrderId(shopifyOrderRequestDto.getOrderID());
        if (shopifyOrder == null) {
            shopifyOrder = new ShopifyOrder();
            shopifyOrder.setOrderId(shopifyOrderRequestDto.getOrderID());
        }
        shopifyOrder.setNote(shopifyOrderRequestDto.getNote().trim());
        shopifyOrder.setSaleReceipt(shopifyOrderRequestDto.getSaleReceipt().trim());
        shopifyOrderRepository.save(shopifyOrder);
    }

    @Override
    public List<ShopifyOrder> getAllShopifyOrders() {
        return shopifyOrderRepository.findAll();
    }


}
