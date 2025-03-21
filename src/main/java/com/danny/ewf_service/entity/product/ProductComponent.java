package com.danny.ewf_service.entity.product;

import com.danny.ewf_service.entity.Component;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "product_components",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "component_id"})
        })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false, referencedColumnName = "id")
    private Component component;

    private Long quantity;

}
