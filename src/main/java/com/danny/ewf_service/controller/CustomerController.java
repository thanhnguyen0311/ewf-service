package com.danny.ewf_service.controller;

import com.danny.ewf_service.payload.request.PhoneNumberRequestDto;
import com.danny.ewf_service.payload.response.CustomerSearchDto;
import com.danny.ewf_service.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;

@RequestMapping("/api/customer")
@RestController
@AllArgsConstructor
public class CustomerController {
    @Autowired
    private final CustomerService customerService;

    @PostMapping("/search")
    public ResponseEntity<List<CustomerSearchDto>> findCustomerByPhone(@RequestBody PhoneNumberRequestDto phoneNumber) {
        try{
            List<CustomerSearchDto> customers = customerService.findCustomersByPartialPhone(phoneNumber.getPhoneNumber());
            return ResponseEntity.ok(customers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
