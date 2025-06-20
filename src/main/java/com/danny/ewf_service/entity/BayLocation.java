package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bayLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LooseInventory> looseInventories = new ArrayList<>();

    @Column(name = "default_sku")
    private String defaultSku;
}
