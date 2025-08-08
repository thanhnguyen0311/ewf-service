package com.danny.ewf_service.payload.response.product;


import jakarta.persistence.Column;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ProductPriceResponseDto {
    private String sku;
    private Double QB1 = 0.0;
    private Double QB2 = 0.0;
    private Double QB3 = 0.0;
    private Double QB4 = 0.0;
    private Double QB5 = 0.0;
    private Double QB6 = 0.0;
    private Double QB7 = 0.0;
    private Double amazonPrice = 0.0;
    private Double ewfdirect = 0.0;
    private Long promotion = 0L;
}
