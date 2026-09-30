package com.danny.ewf_service.payload.response.product;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ProductMetaResponseDto {
    private String sku;
    private String metaTitle;
    private String metaDescription;
}
