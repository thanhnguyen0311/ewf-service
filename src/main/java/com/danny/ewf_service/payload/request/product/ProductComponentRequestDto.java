package com.danny.ewf_service.payload.request.product;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductComponentRequestDto {
    private Long id;
    private Long quantity;
}
