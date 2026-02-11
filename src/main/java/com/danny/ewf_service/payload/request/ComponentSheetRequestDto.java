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
}
