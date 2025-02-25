package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.ICustomerMapper;
import com.danny.ewf_service.entity.Customer;
import com.danny.ewf_service.payload.response.CustomerSearchDto;
import com.danny.ewf_service.repository.CustomerRepository;
import com.danny.ewf_service.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private final CustomerRepository customerRepository;

    @Autowired
    private final ICustomerMapper customerMapper;

    @Override
    public List<CustomerSearchDto> findCustomersByPartialPhone(String phone) {
        List<Customer> customers = customerRepository.findByPartialPhoneNumber(phone);
        System.out.println(customers.size());
        return customerMapper.customerToSearchDtoList(customers);
    }
}
