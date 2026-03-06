package com.danny.ewf_service.payload.response.component;


import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentSheetResponseDto {
    private String sku;
    private String upc;
    private String manufacturer;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
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
    private Long quantityBox;
    private String length;
    private String width;
    private String height;
    private Double weight;
    private Double boxWeight;
    private Double boxLength;
    private Double boxWidth;
    private Double boxHeight;
    private Boolean discontinue;
    private Boolean isDeleted;
}
