package com.danny.ewf_service.payload.request.product;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductPriceRequestDto {
    private String sku;
    private String manualShippingCost;
    private String manualPrice;
}
