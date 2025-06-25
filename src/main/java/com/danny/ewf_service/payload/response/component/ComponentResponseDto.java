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
public class ComponentResponseDto {
    private Long id;
    private String sku;
    private ImageUrls images;
    private String finish;
    private String category;
    private Long inventory;
}
