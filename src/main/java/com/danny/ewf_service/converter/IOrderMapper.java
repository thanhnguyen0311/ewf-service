package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

    @Mapping(target = "id", source="order.id")
    @Mapping(target = "invoiceNumber", source="order.invoiceNumber")
    @Mapping(target = "type", source="order.type")
    @Mapping(target = "orderDate", source="order.orderDate")
    @Mapping(target = "shipDate", source="order.shipDate")
    @Mapping(target = "paymentStatus", source="order.paymentStatus")
    @Mapping(target = "price", source="order.orderPrices.price")
    @Mapping(target = "customerName", source="order.customer.name")
    @Mapping(target = "customerPhone", source="order.customer.phone")
    OrderListResponseDto orderToOrderResponseDto(Order order);
    List<OrderListResponseDto> ordersToOrderResponseDTOs(List<Order> orders);
}
