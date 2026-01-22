package com.danny.ewf_service.entity.wayfair;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wayfair_keyword_report_daily"
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairKeywordReportDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "campaign_id", length = 50, nullable = false)
    private String campaignId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_id",
            referencedColumnName = "campaign_id",
            insertable = false,
            updatable = false
    )
    private WayfairCampaign campaign;

    @Column(name = "keyword_id", nullable = false)
    private Long keywordId;

    @Column(name = "bid", nullable = false)
    private Double bid;

    @Column(name = "search_term")
    private String searchTerm;

    @Column(name = "clicks")
    private Integer clicks = 0;

    @Column(name = "impressions")
    private Integer impressions = 0;

    @Column(name = "spend")
    private Double spend = 0.00;

    @Column(name = "total_sale")
    private Double totalSale = 0.0;

    @Column(name = "order_quantity")
    private Long orderQuantity = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "keyword_id",
            referencedColumnName = "keyword_id",
            insertable = false,
            updatable = false
    )
    private WayfairKeyword keyword;


    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;



}
