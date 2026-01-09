package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairCategoryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;


@Repository
public interface WayfairCategoryReportRepository extends JpaRepository<WayfairCategoryReport,Long> {

    @Query("SELECT w FROM WayfairCategoryReport w " +
           "LEFT JOIN FETCH w.category " +
           "WHERE w.reportDate = :reportDate"
    )
    List<WayfairCategoryReport> findAllByReportDate(@Param("reportDate") LocalDate reportDate);

    Optional<WayfairCategoryReport> findByCategory_TitleAndReportDate(String category_title, LocalDate reportDate);
}
