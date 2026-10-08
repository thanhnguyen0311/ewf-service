package com.danny.ewf_service.payload.request;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class OrderRequestDto {
    String poNumber;
    String carrier;
    String trackingNumber;
    Long quantity;
    String groupSku;
    String contactName;
    String address1;
    String address2;
    String city;
    String state;
    String zip;
    String phone;
    Double prices;
}
