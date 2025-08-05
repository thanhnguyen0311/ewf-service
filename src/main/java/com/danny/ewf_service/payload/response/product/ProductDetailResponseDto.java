package com.danny.ewf_service.payload.response.product;

import com.danny.ewf_service.entity.ImageUrls;
import com.danny.ewf_service.entity.Price;
import com.danny.ewf_service.payload.response.component.ComponentProductDetailResponseDto;
import lombok.*;

import java.util.List;

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
    private String upc;
    private String asin;
    private String title;
    private String localTitle;
    private String description;
    private String htmlDescription;
    private String type;
    private String collection;
    private String shippingMethod;
    private Boolean discontinued = false;
    private String pieces;
    private String order;
    private String category;
    private String mainCategory;
    private String subCategory;
    private Price price;
    private List<ComponentProductDetailResponseDto> components;
    private Boolean amazon = false;
    private Boolean cymax = false;
    private Boolean overstock = false;
    private Boolean wayfair = false;
    private Boolean ewfdirect = false;
    private Boolean houstonDirect = false;
    private Boolean ewfmain = false;
    private String sizeShape;
    private ImageUrls images;
}

