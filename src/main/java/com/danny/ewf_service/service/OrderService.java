package com.danny.ewf_service.service;


import com.danny.ewf_service.entity.Order;
import org.springframework.data.domain.Page;


public interface OrderService {

    Page<Order> getOrdersByPageAndSort (int page);
}
