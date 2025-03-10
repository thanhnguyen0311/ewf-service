package com.danny.ewf_service.payload.response;

import com.danny.ewf_service.entity.ImageUrls;
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
public class ProductResponseDto {
    private Long id;
    private String sku;
    private String localSku;
    private String finish;
    private String category;
    private Long inventory;
    private ImageUrls images;
    private List<ProductResponseDto> subProducts = new ArrayList<>();
    private List<ComponentResponseDto> components = new ArrayList<>();
}
