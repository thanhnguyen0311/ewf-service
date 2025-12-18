package com.danny.ewf_service.entity.wayfair;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "wayfair_campaign_parent_sku")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WayfairCampaignParentSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", referencedColumnName = "campaign_id")
    private WayfairCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_sku", referencedColumnName = "parent_sku")
    private WayfairParentSku parentSku;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
