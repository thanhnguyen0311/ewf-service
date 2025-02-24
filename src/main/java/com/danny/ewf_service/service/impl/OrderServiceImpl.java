package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.repository.OrderRepository;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Page<Order> getOrdersByPageAndSort(int page) {

        PageRequest pageable = PageRequest.of(page, 20, Sort.by("orderDate").descending());
        return orderRepository.findAll(pageable);
    }

}
