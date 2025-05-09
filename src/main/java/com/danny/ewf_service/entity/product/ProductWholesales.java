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
    private Boolean amazon = false;

    @Column(name = "cymax")
    private Boolean cymax = false;

    @Column(name = "wayfair")
    private Boolean wayfair = false;

    @Column(name = "overstock")
    private Boolean overstock = false;

    @Column(name = "ewfdirect")
    private Boolean ewfdirect = false;

    @Column(name = "ewfmain")
    private Boolean ewfmain = false;

    @Column(name = "houstondirect")
    private Boolean houstonDirect = false;
}
