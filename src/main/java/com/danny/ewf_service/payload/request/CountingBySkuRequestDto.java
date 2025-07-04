package com.danny.ewf_service.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class CountingBySkuRequestDto {
    private String sku;
    private String tagIDs;
}
