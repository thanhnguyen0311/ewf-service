package com.danny.ewf_service.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lpnmaster")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WmsLPN {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "epc")
    private String tagID;

    @Column(name = "sku")
    private String sku;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "container")
    private String containerNumber;

    @Column(name = "assigned_location")
    private String bayLocation;

    @Column(name = "status")
    private String status;

    @Column(name = "received_date")
    private LocalDate date;

    @Column(name = "created_on")
    private LocalDateTime createdDate;

    @Column(name = "updated_on")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
