package com.danny.ewf_service.payload.response.product;

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
    private String asin;
    private String upc;
    private Boolean discontinued;
    private Boolean ewfdirect;
    private Boolean amazon;
    private Boolean cymax;
    private Boolean overstock;
    private Boolean wayfair;
    private String localSku;
}
