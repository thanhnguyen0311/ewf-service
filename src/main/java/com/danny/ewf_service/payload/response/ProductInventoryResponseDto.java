package com.danny.ewf_service.payload.response;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ProductInventoryResponseDto {
    private Long id;
    private String sku;
    private Long quantity;
    private String image;
    private Long inProduction;
    private Long inTransit;
    private Long ordered;
    private Long pendingOrders;
    private Boolean discontinued;
    private Boolean amazon;
    private Boolean cymax;
    private Boolean wayfair;
    private Boolean overstock;
}
