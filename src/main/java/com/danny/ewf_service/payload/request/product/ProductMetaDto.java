package com.danny.ewf_service.payload.request.product;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductMetaDto {
    private String title;
    private String description;
    private String sku;
}
