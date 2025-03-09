package com.danny.ewf_service.payload.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class SubProductResponseDto {
    private Long id;
    private String sku;
    private String localSku;
    private String images;
    private String finish;
    private String category;
    private List<ComponentResponseDto> components = new ArrayList<>();
}
