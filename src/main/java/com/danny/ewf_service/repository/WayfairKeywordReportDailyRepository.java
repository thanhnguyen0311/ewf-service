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

}
