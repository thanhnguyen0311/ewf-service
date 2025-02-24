package com.danny.ewf_service.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class OrderImportDto {
    private String type;
    private String phone;
    private String invoiceNumber;
    private String PONumber;
    private String orderDate;
    private String shipDate;
    private String status;
    private String carrier;
    private String sku;
    private Long quantity;
    private Double price;
    private Double priceCheck;
    private String tracking;
}
