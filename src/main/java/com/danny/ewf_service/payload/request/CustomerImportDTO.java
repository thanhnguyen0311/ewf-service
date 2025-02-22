package com.danny.ewf_service.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerImportDTO {
    private String name;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zipcode;
    private String phone;

}
