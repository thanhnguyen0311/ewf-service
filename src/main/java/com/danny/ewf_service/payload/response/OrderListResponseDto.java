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
    private Long id;
    private String invoiceNumber;
    private String type;
    private LocalDateTime orderDate;
    private LocalDateTime shipDate;
    private String carrier;
    private String paymentStatus;
    private Double price;
    private String customerName;
    private String customerPhone;

}
