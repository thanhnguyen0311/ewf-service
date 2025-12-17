package com.danny.ewf_service.entity.wayfair;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wayfair_campaign")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", length = 50, nullable = false, unique = true)
    private String campaignId;

    @Column(name = "campaign_name", length = 255)
    private String campaignName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_b2b")
    private Boolean isB2b;

    @Column(name = "daily_cap")
    private Integer dailyCap = 50;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
