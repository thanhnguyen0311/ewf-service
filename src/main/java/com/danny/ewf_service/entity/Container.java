package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "containers")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Container {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "number")
    private String number;

    @Column(name = "price")
    private Double price;

    @Column(name = "ship_date")
    private LocalDateTime shipDate;

    @Column(name = "arrival_date")
    private LocalDateTime arrivalDate;

    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedDate;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContainerComponent> containerComponents = new ArrayList<>();
}
