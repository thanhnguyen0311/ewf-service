package com.danny.ewf_service.entity.wayfair;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(
        name = "wayfair_ads_report_day",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"report_date", "campaign_id", "parent_sku"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairAdsReportDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "campaign_id", length = 50, nullable = false)
    private String campaignId;

    @Column(name = "parent_sku", length = 80, nullable = false)
    private String parentSku;

    @Column(name = "clicks")
    private Integer clicks = 0;

    @Column(name = "impressions")
    private Integer impressions = 0;

    @Column(name = "spend")
    private Double spend;

    @Column(name = "total_sale")
    private Double totalSale;

    @Column(name = "order_quantity")
    private Long orderQuantity = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(
                    name = "campaign_id",
                    referencedColumnName = "campaign_id",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "parent_sku",
                    referencedColumnName = "parent_sku",
                    insertable = false,
                    updatable = false
            )
    })
    private WayfairCampaignParentSku campaignParentSku;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}