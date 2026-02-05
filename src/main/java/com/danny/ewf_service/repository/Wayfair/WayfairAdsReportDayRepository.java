package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query(value = """
        SELECT
            w.campaign_id AS campaignId,
            w.parent_sku AS parentSku,
            COALESCE(SUM(w.clicks), 0) AS totalClicks,
            COALESCE(SUM(w.impressions), 0) AS totalImpressions,
            COALESCE(SUM(w.spend), 0) AS totalSpend,
            COALESCE(SUM(w.total_sale), 0) AS totalSales,
            COALESCE(SUM(w.order_quantity), 0) AS totalOrderQuantity,
            p.default_bid,
            c.campaign_name AS campaignName,
            p.products,
            cat.title AS categoryTitle
        FROM
            wayfair_ads_report_daily w
            INNER JOIN wayfair_campaign c ON w.campaign_id = c.campaign_id
            INNER JOIN wayfair_parent_sku p ON w.parent_sku = p.parent_sku
            LEFT JOIN wayfair_category cat ON c.category_id = cat.id
        WHERE
            w.report_date BETWEEN :fromDate AND :toDate
            AND c.type = 'Product'
        GROUP BY
            w.campaign_id, w.parent_sku
        ORDER BY
            w.campaign_id, w.parent_sku
        """, nativeQuery = true)
    List<Object[]> getAggregatedReportsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Modifying
    @Transactional
    @Query("DELETE FROM WayfairAdsReportDay w WHERE w.reportDate = :reportDate")
    int removeByDateRange(
            @Param("reportDate") LocalDate reportDate
    );

    List<WayfairAdsReportDay> findAllByReportDateIn(Collection<LocalDate> dates);

    @Query("SELECT r.reportDate, r.campaignId, r.parentSku, r.isB2b FROM WayfairAdsReportDay r WHERE r.reportDate BETWEEN :fromDate AND :toDate")
    List<Object[]> findReportKeysInDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query("SELECT MAX(w.reportDate) FROM WayfairAdsReportDay w")
    LocalDate findNewestReportDate();

    @Query("SELECT w FROM WayfairAdsReportDay w " +
           "JOIN FETCH w.campaignParentSku cps " +
           "JOIN FETCH cps.campaign c " +
           "JOIN FETCH cps.parentSku ps " +
           "JOIN FETCH c.category " +
           "WHERE w.reportDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.reportDate DESC")
    List<WayfairAdsReportDay> findAllWithJoinFetchBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
