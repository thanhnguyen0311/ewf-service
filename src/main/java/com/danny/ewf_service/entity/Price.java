package com.danny.ewf_service.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prices")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "QB1")
    private Double QB1;

    @Column(name = "QB2")
    private Double QB2;

    @Column(name = "QB3")
    private Double QB3;

    @Column(name = "QB4")
    private Double QB4;

    @Column(name = "QB5")
    private Double QB5;

    @Column(name = "QB6")
    private Double QB6;

    @Column(name = "QB7")
    private Double QB7;

    @Column(name = "amazon")
    private Double amazonPrice;

    @Column(name = "QB2025")
    private Double QB2025;

    @Column(name = "ewfdirect")
    private Double ewfdirect;

    @Column(name = "ewfdirect_manual_price")
    private Double ewfdirectManualPrice;

    @Column(name = "promotion")
    private Long promotion;

    @Column(name = "shipping_cost")
    private Double shippingCost;

}
