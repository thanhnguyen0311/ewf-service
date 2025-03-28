package com.danny.ewf_service.payload.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ProductDetailResponseDto {

    private Long id;
    private String sku;
    private String localSku;
    private String image;
    private String upc;
    private String asin;
    private String title;
    private String localTitle;
    private String description;
    private String type;
    private String shippingMethod;
    private Boolean discontinued;
    private String pieces;
    private List<Map<String, Long>> components;
}
