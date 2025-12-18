package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WayfairAdsReportDayRepository  extends JpaRepository<WayfairAdsReportDay, Long> {

    boolean existsByReportDateAndCampaignIdAndParentSku(
            LocalDate reportDate, String campaignId, String parentSku
    );

}
