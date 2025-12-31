package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WayfairAdsReportDayRepository  extends JpaRepository<WayfairAdsReportDay, Long> {

    boolean existsByReportDateAndCampaignIdAndParentSku(
            LocalDate reportDate, String campaignId, String parentSku
    );

    @Query("SELECT w FROM WayfairAdsReportDay w WHERE w.reportDate = :reportDate AND w.campaignId = :campaignId AND w.parentSku = :parentSku")
    Optional<WayfairAdsReportDay> findByReportDateAndCampaignIdAndParentSku(
            @Param("reportDate") LocalDate reportDate,
            @Param("campaignId") String campaignId,
            @Param("parentSku") String parentSku
    );

    @Query("""
            SELECT
              w.campaignId,
              w.parentSku,
              COALESCE(SUM(w.clicks), 0),
              COALESCE(SUM(w.impressions), 0),
              COALESCE(SUM(w.spend), 0),
              COALESCE(SUM(w.totalSale), 0),
              COALESCE(SUM(w.orderQuantity), 0),
              w.campaignParentSku.parentSku.defaultBid,
              MAX(
                 CASE
                 WHEN w.reportDate = :toDate THEN w.bid
                 ELSE NULL
                 END
              )
            FROM WayfairAdsReportDay w
            WHERE w.reportDate BETWEEN :fromDate AND :toDate
            GROUP BY w.campaignId, w.parentSku, w.campaignParentSku.parentSku.defaultBid
            ORDER BY w.campaignId, w.parentSku
            """)
    List<Object[]> getAggregatedReportsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    List<WayfairAdsReportDay> findAllByReportDateIn(Collection<LocalDate> dates);


}
