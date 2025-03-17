package com.danny.ewf_service.entity;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Data
public class ProductWithQuantity {
    private Product product;
    private Long quantity;

}
