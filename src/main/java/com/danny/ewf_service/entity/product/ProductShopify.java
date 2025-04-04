package com.danny.ewf_service.entity.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shopify")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProductShopify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "houston")
    private String houston;

    @Column(name = "ewfdirect")
    private String ewfdirect;

    @Column(name = "ewfmain")
    private String ewfmain;

}
