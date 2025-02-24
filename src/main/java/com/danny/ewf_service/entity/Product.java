package com.danny.ewf_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name="price")
    private Long price;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "local_id", referencedColumnName = "id")
    @JsonIgnore
    private LocalProduct localProduct;

    @Column(name = "finish")
    private String finish;


    @Column(name = "images")
    private String images;

}
