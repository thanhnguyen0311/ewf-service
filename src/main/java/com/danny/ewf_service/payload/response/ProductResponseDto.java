package com.danny.ewf_service.payload.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductResponseDto {
    private Long id;
    private String sku;
    private Long price;
    private Long localPrice;
    private String images;
}
