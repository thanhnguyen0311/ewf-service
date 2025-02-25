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
public class Dimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_shape")
    private String sizeShape;

}
