package com.danny.ewf_service.payload.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class CustomerSearchDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
