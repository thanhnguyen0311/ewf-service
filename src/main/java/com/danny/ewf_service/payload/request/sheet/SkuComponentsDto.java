package com.danny.ewf_service.payload.request.sheet;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Setter
@Getter
@ToString
public class SkuComponentsDto {
    private String productSku;
    private List<ComponentItemDto> components;
}
