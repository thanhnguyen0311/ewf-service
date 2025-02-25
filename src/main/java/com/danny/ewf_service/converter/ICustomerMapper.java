package com.danny.ewf_service.converter;

import com.danny.ewf_service.entity.Customer;
import com.danny.ewf_service.payload.response.CustomerSearchDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ICustomerMapper {

    @Mapping(target = "id", source = "customer.id")
    @Mapping(target = "name", source = "customer.name")
    @Mapping(target = "phone", source = "customer.phone")
    @Mapping(target = "email", source = "customer.email")
    @Mapping(target = "address", source = "customer.address")
    @Mapping(target = "city", source = "customer.city")
    @Mapping(target = "state", source = "customer.state")
    @Mapping(target = "zipCode", source = "customer.zipCode")
    CustomerSearchDto customerToSearchDto(Customer customer);
    List<CustomerSearchDto> customerToSearchDtoList(List<Customer> customers);
}
