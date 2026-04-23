package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.IOrderMapper;
import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import com.danny.ewf_service.repository.OrderRepository;
import com.danny.ewf_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final IOrderMapper IOrderMapper;

    @Override
    public List<OrderListResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return IOrderMapper.orderToOrderListResponseDtos(orders);
    }
}
