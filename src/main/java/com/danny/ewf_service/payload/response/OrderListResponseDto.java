package com.danny.ewf_service.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class OrderListResponseDto {
    private String customer;
    private String accountNumber;
    private String poNumber;
    private String masterTrackingNumber;
    private String trackingNumber;
    private String status;
    private String groupSku;
    private String contactName;
    private String address1;
    private String address2;
    private String zipcode;
    private String phone;
    private String city;
    private String state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
