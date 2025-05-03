package com.danny.ewf_service.entity.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_wholesales")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProductWholesales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amazon")
    private Boolean amazon;

    @Column(name = "cymax")
    private Boolean cymax;

    @Column(name = "wayfair")
    private Boolean wayfair;

    @Column(name = "overstock")
    private Boolean overstock;

    @Column(name = "ewfdirect")
    private Boolean ewfdirect;

    @Column(name = "ewfmain")
    private Boolean ewfmain;

    @Column(name = "houstondirect")
    private Boolean houstonDirect;
}
