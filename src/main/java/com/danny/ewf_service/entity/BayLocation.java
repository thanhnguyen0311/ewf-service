package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bay_location")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BayLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bay_code")
    private String bayCode;

    @Column(name = "zone")
    private String zone;

    @Column(name = "max_pallets")
    private Long maxPallets;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "default_sku")
    private String defaultSku;
}
