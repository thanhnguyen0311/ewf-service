package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Setter
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "description")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_id", referencedColumnName = "id")
    private LocalProduct localProduct;


    @Column(name = "images")
    private String images;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public String getImages() {
        return images;
    }

    public LocalProduct getLocalProduct() {
        return localProduct;
    }
}
