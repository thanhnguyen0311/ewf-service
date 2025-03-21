package com.danny.ewf_service.entity.product;

import com.danny.ewf_service.entity.Price;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "local_id", referencedColumnName = "id")
    private LocalProduct localProduct;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComponent> productComponents = new ArrayList<>();

    @Column(name = "finish")
    private String finish;

    @Column(name = "shipping")
    private String shippingMethod;

    @Column(name = "discontinued")
    private Boolean discontinued;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "wholesales_id", referencedColumnName = "id")
    private ProductWholesales wholesales = new ProductWholesales();


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "price_id", referencedColumnName = "id")
    private Price price;

    @Column(name = "title")
    private String title;

    @Column(name = "images")
    private String images;

}
