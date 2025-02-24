package com.danny.ewf_service.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class CustomerImportDto {
    private String name;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zipcode;
    private String phone;

}
