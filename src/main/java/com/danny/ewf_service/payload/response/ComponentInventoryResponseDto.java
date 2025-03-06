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
    private String sku;
    private Long name;
    private String image;
    private String inventory;
    
}
