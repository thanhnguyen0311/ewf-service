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
          w.campaignParentSku.campaign.campaignName,
          w.campaignParentSku.parentSku.productName,
          w.campaignParentSku.parentSku.products,
          w.campaignParentSku.parentSku.className,
          w.campaignParentSku.campaign.startDate,
          w.campaignParentSku.campaign.dailyCap,
          w.campaignParentSku.campaign.category.title
        FROM WayfairAdsReportDay w
        WHERE w.reportDate BETWEEN :fromDate AND :toDate
          AND w.campaignParentSku.campaign.type = 'Product'
        GROUP BY w.campaignId, w.parentSku
        ORDER BY w.campaignId, w.parentSku
        """)
    List<Object[]> getAggregatedReportsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    List<WayfairAdsReportDay> findAllByReportDateIn(Collection<LocalDate> dates);

    @Query("SELECT r.reportDate, r.campaignId, r.parentSku, r.isB2b FROM WayfairAdsReportDay r WHERE r.reportDate BETWEEN :fromDate AND :toDate")
    List<Object[]> findReportKeysInDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query("SELECT MAX(w.reportDate) FROM WayfairAdsReportDay w")
    LocalDate findNewestReportDate();


}
