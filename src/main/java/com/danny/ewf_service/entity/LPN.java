package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lpn")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LPN {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfid_tag_id")
    private String tagID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sku_id", referencedColumnName = "id", nullable = false)
    private Component component;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "container_number")
    private String containerNumber;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bay_id", referencedColumnName = "id")
    private BayLocation bayLocation;

    @Column(name = "status")
    private String status;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "created_at")
    private LocalDateTime createdDate;
}
