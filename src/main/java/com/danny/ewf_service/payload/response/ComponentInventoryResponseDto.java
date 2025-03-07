package com.danny.ewf_service.payload.response;

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
    private String manufacture;
    private String sku;
    private Long name;
    private String image;
    private Long inventory;
    private String category;
    private Long onPO;
    private Long inTransit;
    private Long inventoryVN;
    private Boolean discontinue;
}
