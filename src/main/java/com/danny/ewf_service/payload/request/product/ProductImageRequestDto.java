package com.danny.ewf_service.payload.request.product;


import com.danny.ewf_service.entity.ImageUrls;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductImageRequestDto {
    private Long id;
    private ImageUrls images;
}
