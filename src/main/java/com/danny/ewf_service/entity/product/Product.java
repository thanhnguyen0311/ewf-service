package com.danny.ewf_service.entity.product;

import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.Price;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Setter
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductComponent> components;

    @Column(name = "shipping")
    private String shippingMethod;

    @Column(name = "local_title")
    private String localTitle;

    @Column(name = "cat")
    private String category;

    @Column(name = "cat2")
    private String category2;

    @Column(name = "`order`")
    private String order;

    @Column(name = "name")
    private String name;

    @Column(name = "local_sku")
    private String localSku;

    @Column(name = "discontinued")
    private Boolean discontinued;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "wholesales_id", referencedColumnName = "id")
    private ProductWholesales wholesales;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "price_id", referencedColumnName = "id")
    private Price price;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "dimension_id", referencedColumnName = "id")
    private Dimension dimension;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "detail_id", referencedColumnName = "id")
    private ProductDetail productDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "metadata_id", referencedColumnName = "id")
    private ProductMetadata metadata;

    @Column(name = "title")
    private String title;

    @Column(name = "images")
    private String images;

    @Column(name = "upc")
    private String upc;

    @Column(name = "asin")
    private String asin;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
