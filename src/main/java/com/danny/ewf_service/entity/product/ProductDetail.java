package com.danny.ewf_service.entity.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_details")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProductDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "html_description")
    private String htmlDescription;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "product_types")
    private String productType;

    @Column(name = "pieces")
    private String pieces;

    @Column(name = "collection")
    private String collection;

    @Column(name = "main_category")
    private String mainCategory;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "size_shape")
    private String sizeShape;

    @Column(name = "finish")
    private String finish;

    @Column(name = "style")
    private String stype;
}
