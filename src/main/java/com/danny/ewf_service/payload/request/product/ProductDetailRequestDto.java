package com.danny.ewf_service.payload.request.product;

import com.danny.ewf_service.entity.ImageUrls;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductDetailRequestDto {
    private Long id;
    private String upc;
    private String asin;
    private String title;
    private String localTitle;
    private String description;
    private String htmlDescription;
    private String type;
    private String collection;
    private String finish;
    private String order;
    private String category;
    private String mainCategory;
    private String subCategory;
    private String shippingMethod;
    private String pieces;
    private Boolean discontinued;
    private Boolean amazon;
    private Boolean cymax;
    private Boolean overstock;
    private Boolean wayfair;
    private Boolean ewfdirect;
    private Boolean houstondirect;
    private Boolean ewfmain;
    private String sizeShape;
    private List<ProductComponentRequestDto> components;
    private ImageUrls images;
    private Double ewfdirectManualPrice;
    private Long promotion;
    private String dimension;
}