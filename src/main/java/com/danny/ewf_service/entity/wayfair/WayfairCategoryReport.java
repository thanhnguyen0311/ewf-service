package com.danny.ewf_service.entity.wayfair;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "wayfair_category_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"report_date", "category_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairCategoryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
