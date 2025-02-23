package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "phone")
    private String phone;

    @Column(name = "price_level")
    private Long priceLevel;

    @Column(name = "zipcode")
    private String zipCode;

    @Column(name = "state")
    private String state;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "address2")
    private String address2;
}
