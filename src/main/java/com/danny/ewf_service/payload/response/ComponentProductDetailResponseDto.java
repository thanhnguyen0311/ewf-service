package com.danny.ewf_service.payload.response;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ComponentProductDetailResponseDto{
    private Long id;
    private Long componentId;
    private String sku;
    private Long quantity;
}
