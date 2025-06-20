package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loose_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bay_id", "component_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LooseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private BayLocation bayLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(name = "quantity")
    private Long quantity = 0L;
}