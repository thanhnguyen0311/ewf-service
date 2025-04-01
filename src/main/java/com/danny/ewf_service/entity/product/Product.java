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

    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductComponent> components = new ArrayList<>();

    @Column(name = "shipping")
    private String shippingMethod;

    @Column(name = "local_title")
    private String localTitle;

    @Column(name = "cat")
    private String category;

    @Column(name = "order")
    private String order;

    @Column(name = "local_sku")
    private String localSku;

    @Column(name = "discontinued")
    private Boolean discontinued;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "wholesales_id", referencedColumnName = "id")
    private ProductWholesales wholesales = new ProductWholesales();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "price_id", referencedColumnName = "id")
    private Price price;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "detail_id", referencedColumnName = "id")
    private ProductDetail productDetail;

    @Column(name = "title")
    private String title;

    @Column(name = "images")
    private String images;

    @Column(name = "upc")
    private String upc;

    @Column(name = "asin")
    private String asin;

}
