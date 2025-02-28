package com.danny.ewf_service.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductInventorySearchRequestDto {
    private int page;
    private String sku;
}
