package com.danny.ewf_service.payload.response.component;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentInboundResponseDto {
    private String sku;
    private String upc;
    private Long palletCapacity;
}
