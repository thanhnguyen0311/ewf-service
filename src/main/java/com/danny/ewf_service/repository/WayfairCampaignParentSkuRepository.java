package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WayfairCampaignParentSkuRepository extends JpaRepository<WayfairCampaignParentSku, Long> {

    List<WayfairCampaignParentSku> findAllByCampaignIsActiveIsTrue();
}
