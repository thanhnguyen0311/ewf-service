package com.danny.ewf_service.payload.request.sheet;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Setter
@Getter
@ToString
public class ComponentItemDto {
    private String sku;
    private Long pcs;
}
