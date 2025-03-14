package com.danny.ewf_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "container_component",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"container_id", "component_id"})
        })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "container_id", nullable = false, referencedColumnName = "id")
    private Container container;

    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false, referencedColumnName = "id")
    private Component component;

    private Long quantity;
}
