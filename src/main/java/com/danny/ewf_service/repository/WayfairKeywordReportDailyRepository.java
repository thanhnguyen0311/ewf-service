package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairKeywordReportDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WayfairKeywordReportDailyRepository extends JpaRepository<WayfairKeywordReportDaily, Long> {

    @Query("SELECT r.reportDate, r.campaignId, r.keywordId, r.searchTerm FROM WayfairKeywordReportDaily r WHERE r.reportDate BETWEEN :fromDate AND :toDate")
    List<Object[]> findReportKeysInDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);



    @Query("""
        SELECT
          w.campaignId,
          w.keywordId,
          w.keyword.keywordValue,
          w.keyword.type,
          COALESCE(SUM(w.clicks), 0),
          COALESCE(SUM(w.impressions), 0),
          COALESCE(SUM(w.spend), 0),
          COALESCE(SUM(w.totalSale), 0),
          COALESCE(SUM(w.orderQuantity), 0),
          w.keyword.defaultBid,
          w.campaign.campaignName,
          w.campaign.startDate,
          w.campaign.dailyCap
        FROM WayfairKeywordReportDaily w
        WHERE w.reportDate BETWEEN :fromDate AND :toDate
          AND w.campaign.type = 'Keyword'
        GROUP BY w.campaignId, w.keywordId
        ORDER BY w.campaignId, w.keywordId
        """)
    List<Object[]> getAggregatedReportsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
