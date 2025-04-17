package com.danny.ewf_service.payload.request;

import lombok.*;

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
}