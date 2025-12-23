package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WayfairAdsReportDayRepository  extends JpaRepository<WayfairAdsReportDay, Long> {

    boolean existsByReportDateAndCampaignIdAndParentSku(
            LocalDate reportDate, String campaignId, String parentSku
    );

    @Query("SELECT " +
           "w.campaignId, w.parentSku, " +
           "COALESCE(SUM(w.clicks), 0), " +
           "COALESCE(SUM(w.impressions), 0), " +
           "COALESCE(SUM(w.spend), 0), " +
           "COALESCE(SUM(w.totalSale), 0), " +
           "COALESCE(SUM(w.orderQuantity), 0) " +
           "FROM WayfairAdsReportDay w " +
           "WHERE w.reportDate BETWEEN :fromDate AND :toDate " +
           "GROUP BY w.campaignId, w.parentSku " +
           "ORDER BY w.campaignId, w.parentSku")
    List<Object[]> getAggregatedReportsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
