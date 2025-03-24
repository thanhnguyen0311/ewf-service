package com.danny.ewf_service.entity.product; // Changed package name to lowercase "product"

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "local")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LocalProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price")
    private Long price;

    @Column(name = "local_title")
    private String localTitle;

    @Column(name = "local_sku")
    private String localSku;

    @OneToOne(mappedBy = "localProduct")
    private Product product; // Fully qualified name to resolve ambiguity

}
