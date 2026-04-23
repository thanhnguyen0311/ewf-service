package com.danny.ewf_service.service;


import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;


public interface OrderService {

    List<OrderListResponseDto> getAllOrders();
}
