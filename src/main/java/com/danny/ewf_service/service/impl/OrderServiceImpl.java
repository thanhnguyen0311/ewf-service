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
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final IOrderMapper IOrderMapper;

    @Override
    public List<OrderListResponseDto> getAllOrders() {
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Order> orderPage = orderRepository.findAll(pageRequest);

        List<Order> orders = orderPage.getContent();


        return IOrderMapper.orderToOrderListResponseDtos(orders);



    }
    public List<OrderListResponseDto> sortOrdersByUpdatedAt(List < OrderListResponseDto> orderDtoList) {
        return orderDtoList.stream()
                .sorted(Comparator.comparing(OrderListResponseDto::getUpdatedAt).reversed()) // Sort by updatedAt in descending order
                .collect(Collectors.toList());
    }
}
