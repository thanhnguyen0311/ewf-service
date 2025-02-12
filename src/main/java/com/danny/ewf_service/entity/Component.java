package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "components")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Component {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "box")
    private Long box;

    @Column(name = "finish")
    private String finish;

    @Column(name = "size_shape")
    private String sizeShape;

    @Column(name = "dims")
    private String dims;

    @Column(name = "inventory")
    private Long inventory;

    @Column(name = "box_dims")
    private String box_dims;

    @Column(name = "type")
    private String type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
}
