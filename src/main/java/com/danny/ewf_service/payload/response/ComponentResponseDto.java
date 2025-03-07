package com.danny.ewf_service.payload.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class ComponentResponseDto {
    private Long id;
    private String sku;
    private String image;
    private String finish;
    private String category;
    private Long inventory;
}
