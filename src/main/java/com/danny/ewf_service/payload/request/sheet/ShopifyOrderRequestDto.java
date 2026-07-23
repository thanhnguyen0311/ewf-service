package com.danny.ewf_service.payload.request.sheet;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Setter
@Getter
@ToString
public class ShopifyOrderRequestDto {
    private String orderID;
    private String note;
    private String saleReceipt;
}
