package com.danny.ewf_service.payload.request.product;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ProductSheetRequestDto {
    private String sku;
    private String category;
    private String mainCategory;
    private String subCategory;
    private String upc;
    private String asin;
    private String parentAsin;
    private String amzVariationID;
    private String wayfairVariationID;
    private String shipping;
    private Boolean discontinued;
    private String title;
    private String finish;
    private String sizeShape;
    private String collection;
    private String shortTitle;
    private String bulletPoint1;
    private String bulletPoint2;
    private String bulletPoint3;
    private String bulletPoint4;
    private String bulletPoint5;
    private String description;
    private String htmlDescription;
}
