package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.ShopifyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopifyOrderRepository extends JpaRepository<ShopifyOrder,Long> {

    ShopifyOrder findByOrderId(String orderId);
}
