package com.danny.ewf_service.repository.Wayfair;

import com.danny.ewf_service.entity.wayfair.WayfairCategoryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface WayfairCategoryReportRepository extends JpaRepository<WayfairCategoryReport,Long> {

    Optional<WayfairCategoryReport> findByCategory_TitleAndReportDate(String category_title, LocalDate reportDate);
}
