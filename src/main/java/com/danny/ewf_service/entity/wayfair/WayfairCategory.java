package com.danny.ewf_service.entity.wayfair;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wayfair_category")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

}
