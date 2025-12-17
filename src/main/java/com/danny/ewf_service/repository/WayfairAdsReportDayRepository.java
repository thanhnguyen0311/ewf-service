package com.danny.ewf_service.repository;

import com.danny.ewf_service.entity.wayfair.WayfairAdsReportDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WayfairAdsReportDayRepository  extends JpaRepository<WayfairAdsReportDay, Long> {


}
