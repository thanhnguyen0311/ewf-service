package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Order;
import com.danny.ewf_service.payload.response.OrderListResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

    @Mapping(target = "customer", source = "order.customer")
    @Mapping(target = "accountNumber", source = "order.accountNumber")
    @Mapping(target = "poNumber", source = "order.poNumber")
    @Mapping(target = "masterTrackingNumber", source = "order.masterTrackingNumber")
    @Mapping(target = "trackingNumber", source = "order.trackingNumber")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "groupSku", source = "order.groupSku")
    @Mapping(target = "contactName", source = "order.contactName")
    @Mapping(target = "address1", source = "order.address1")
    @Mapping(target = "address2", source = "order.address2")
    @Mapping(target = "zipcode", source = "order.zipcode")
    @Mapping(target = "phone", source = "order.phone")
    @Mapping(target = "city", source = "order.city")
    @Mapping(target = "state", source = "order.state")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "updatedAt", source = "order.updatedAt")
    OrderListResponseDto orderToOrderListResponseDto(Order order);
    List<OrderListResponseDto> orderToOrderListResponseDtos(List<Order> orders);
}
