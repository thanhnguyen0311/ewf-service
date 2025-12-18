package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WayfairAdsReportDayRepository  extends JpaRepository<WayfairAdsReportDay, Long> {

    boolean existsByReportDateAndCampaignIdAndParentSku(
            LocalDate reportDate, String campaignId, String parentSku
    );

    @Query("SELECT COALESCE(SUM(w.clicks), 0) FROM WayfairAdsReportDay w " +
           "WHERE w.reportDate BETWEEN :fromDate AND :toDate " +
           "AND w.parentSku = :parentSku " +
           "AND w.campaignId = :campaignId")
    Long sumClicksByDateRangeAndParentSkuAndCampaignId(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("parentSku") String parentSku,
            @Param("campaignId") String campaignId
    );

}
