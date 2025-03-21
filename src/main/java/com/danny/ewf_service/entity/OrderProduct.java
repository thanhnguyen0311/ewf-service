package com.danny.ewf_service.entity;

import com.danny.ewf_service.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_product",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "product_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false, referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
    private Product product;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "order_price")
    private Long orderPrice;

}