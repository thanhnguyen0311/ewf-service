package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.CustomerSearchDto;

import java.util.List;

public interface CustomerService {

    List<CustomerSearchDto> findCustomersByPartialPhone (String phone);
}
