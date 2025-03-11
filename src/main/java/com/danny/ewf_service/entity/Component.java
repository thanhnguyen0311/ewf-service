package com.danny.ewf_service.entity;

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
@ToString
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "dimensions_id", referencedColumnName = "id")
    private Dimension dimensions;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    private Report report = new Report();

    @Column(name = "inventory")
    private Long inventory = 0L;

    @Column(name = "type")
    private String type;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "discontinue")
    private Boolean discontinue;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComponent> productComponents = new ArrayList<>();
}
