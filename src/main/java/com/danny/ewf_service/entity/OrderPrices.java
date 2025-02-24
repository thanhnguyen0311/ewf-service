package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_price")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderPrices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price")
    private Double price;

    @Column(name = "price_check")
    private Double priceCheck;

    @Column(name = "tax")
    private Double tax;

    @Column(name = "shipping_cost")
    private Double shippingCost;

}
