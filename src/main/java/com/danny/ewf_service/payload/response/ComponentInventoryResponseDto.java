package com.danny.ewf_service.payload.response;

import com.danny.ewf_service.entity.ImageUrls;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentInventoryResponseDto {
    private Long id;
    private String manufacturer;
    private String sku;
    private String name;
    private ImageUrls images;
    private Long inventory;
    private String category;
    private String finish;
    private Boolean discontinue;
}
