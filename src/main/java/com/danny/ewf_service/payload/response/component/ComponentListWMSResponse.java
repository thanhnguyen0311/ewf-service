package com.danny.ewf_service.payload.response.component;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.ImageUrls;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class ComponentListWMSResponse {
    private Long id;
    private String sku;
    private String upc;
    private String name;
    private Dimension dimension;
    private String manufacturer;
    private String type;
    private List<String> images;
}
