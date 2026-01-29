package com.danny.ewf_service.entity.wayfair;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private WayfairCategory category;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "ad_spend")
    private Double adSpend;

    @Column(name = "sale_by_ads")
    private Double saleByAds;

    @Column(name = "acos")
    private Double acos;

    @Column(name = "target_acos")
    private Double targetAcos;

    @Column(name = "order_quantity")
    private Integer orderQuantity;

    @Column(name = "impressions")
    private Long impressions;

    @Column(name = "clicks")
    private Long clicks;
}
