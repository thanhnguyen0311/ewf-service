package com.danny.ewf_service.payload.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductSearchResponseDto {
    private Long id;
    private String sku;
    private String image;
    private String finish;
}
