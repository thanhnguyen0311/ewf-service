package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer")
    private String customer;

    @Column(name = "po_number", unique = true)
    private String poNumber;

    @Column(name = "master_tracking_number")
    private String masterTrackingNumber;

    @Column(name = "tracking_number", columnDefinition = "TEXT")
    private String trackingNumber;

    @Column(name = "status")
    private String status;

    @Column(name = "group_sku")
    private String groupSku;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "zipcode", length = 50)
    private String zipcode;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "channel", length = 100)
    private String channel = "unknown";

    @Column(name = "carrier", length = 255)
    private String carrier;

    @Column(name = "quantity")
    private Long quantity = 1L;

    @Column(name = "prices")
    private Double totalPrice;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
