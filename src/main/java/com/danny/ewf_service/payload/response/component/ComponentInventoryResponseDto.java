package com.danny.ewf_service.payload.response.component;

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
    private Long salesReport;
    private String sku;
    private String name;
    private ImageUrls images;
    private Long inventory;
    private String category;
    private String finish;
    private Boolean discontinue;
    private Long report120Days = 0L;
    private Long inStock = 0L;
    private Long onPO = 0L;
    private Long inTransit = 0L;
    private Double rating = 0.0;
    private Long toShip = 0L;
    private Long stockVN = 0L;
    private Long inProduction = 0L;
    private String stockStatus = "";
    private String toBeShipped;
}
