package com.danny.ewf_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dimensions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Dimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_shape")
    private String sizeShape;

    @Column(name = "quantity_box")
    private Long quantityBox;

    @Column(name = "box_weight")
    private Double boxWeight;

    @Column(name = "box_height")
    private Double boxHeight;

    @Column(name = "box_length")
    private Double boxLength;

    @Column(name = "box_width")
    private Double boxWidth;


}
