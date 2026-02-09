package com.danny.ewf_service.entity;

import com.danny.ewf_service.entity.product.ProductComponent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "components")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @Column(name = "name")
    private String name;

    @Column(name = "finish")
    private String finish;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "category")
    private String category;

    @Column(name = "images")
    private String images;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    private Report report = new Report();

    @Column(name = "inventory")
    private Long inventory = 0L;

    @Column(name = "type")
    private String type = "";

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "price_id", referencedColumnName = "id")
    private Price price;

    @Column(name = "upc")
    private String upc;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "discontinue")
    private Boolean discontinue;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "dimensions_id", referencedColumnName = "id")
    private Dimension dimension;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComponent> productComponents = new ArrayList<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContainerComponent> containerComponents = new ArrayList<>();


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LooseInventory> looseInventories = new ArrayList<>();

    @Column(name = "pos")
    private Long pos;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "fabric_color")
    private String fabricColor = "";

    @Column(name = "fabric_code")
    private String fabricCode = "";

    @Column(name = "size_shape")
    private String sizeShape = "";

    @Column(name = "collection")
    private String collection = "";

}
