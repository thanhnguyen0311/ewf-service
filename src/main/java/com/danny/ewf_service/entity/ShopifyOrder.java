package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shopify_orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Setter
public class ShopifyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "note")
    private String note;

    @Column(name = "sale_receipt")
    private String saleReceipt;
}
