package com.danny.ewf_service.payload.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentInventoryRequestDto {
    private Long id;
    private String name;
    private Long inventory;
    private Boolean discontinue;
    private Long toShip;
    private Long onPO;
    private Long inTransit;
    private Long stockVN;
    private Long inProduction;
}
