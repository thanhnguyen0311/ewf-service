package com.danny.ewf_service.payload.request;

import lombok.*;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentSheetRequestDto {
    private String sku;
    private String upc;
    private String manufacturer;
    private String type;
    private String finish;
    private String category;
    private String name;
    private String fabricColor;
    private String fabricCode;
    private String sizeShape;
    private String collection;
    private String style;
    private String shippingMethod;
    private String quantityBox;
    private String length;
    private String width;
    private String height;
    private String weight;
    private String boxLength;
    private String boxWidth;
    private String boxHeight;
    private String boxWeight;
    private Boolean isDeleted;
    private String inventory;
    private String imageLink;
}
