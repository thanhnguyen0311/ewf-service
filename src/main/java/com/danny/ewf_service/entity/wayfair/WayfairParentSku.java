package com.danny.ewf_service.entity.wayfair;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wayfair_parent_sku")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairParentSku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_sku", length = 80, nullable = false, unique = true)
    private String parentSku;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "default_bid")
    private Float defaultBid = 0f;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "products", length = 255)
    private String products; // (note: denormalized display/cached field)

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // getters/setters
}
