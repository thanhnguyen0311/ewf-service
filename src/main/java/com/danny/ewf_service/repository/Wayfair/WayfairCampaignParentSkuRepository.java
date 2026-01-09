package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WayfairCampaignParentSkuRepository extends JpaRepository<WayfairCampaignParentSku, Long> {
    @Query("SELECT wcps FROM WayfairCampaignParentSku wcps WHERE wcps.campaign.isActive = true AND wcps.campaign.isB2b = false")
    List<WayfairCampaignParentSku> findAllCampaign();

    boolean existsByCampaignCampaignIdAndParentSkuParentSku(String campaignId, String parentSku);

    WayfairCampaignParentSku findByCampaignCampaignIdAndParentSkuParentSku(String campaignId, String parentSku);
}
