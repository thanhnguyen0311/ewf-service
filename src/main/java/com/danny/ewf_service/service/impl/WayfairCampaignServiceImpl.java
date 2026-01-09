package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.wayfair.WayfairCampaign;
import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.entity.wayfair.WayfairCategory;
import com.danny.ewf_service.entity.wayfair.WayfairCategoryReport;
import com.danny.ewf_service.exception.ResourceNotFoundException;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignCategoryDto;
import com.danny.ewf_service.payload.request.campaign.WayfairCategoryReportRequestDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.payload.response.campaign.WayfairKeywordReportDto;
import com.danny.ewf_service.repository.Wayfair.*;
import com.danny.ewf_service.service.WayfairCampaignService;
import com.danny.ewf_service.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class WayfairCampaignServiceImpl implements WayfairCampaignService {

    @Autowired
    private final WayfairCampaignParentSkuRepository wayfairCampaignParentSkuRepository;

    @Autowired
    private final WayfairAdsReportDayRepository wayfairAdsReportDayRepository;

    @Autowired
    private final DateTimeUtils dateTimeUtils;

    @Autowired
    private final WayfairKeywordReportDailyRepository wayfairKeywordReportDailyRepository;

    @Autowired
    private final WayfairCampaignRepository wayfairCampaignRepository;

    @Autowired
    private final WayfairCategoryRepository wayfairCategoryRepository;

    @Autowired
    private final WayfairCategoryReportRepository wayfairCategoryReportRepository;

    @Override
    public List<WayfairCampaignParentSku> findAllActiveCampaignsWithParentSkus() {
        return wayfairCampaignParentSkuRepository.findAllCampaign();
    }

    @Override
    public List<WayfairAdsReportDto> sumClicksByDateRangeAndParentSkuAndCampaignId(String startDate , String endDate) {
        List<WayfairAdsReportDto> wayfairAdsReportDtos = new ArrayList<>();
        LocalDate start = dateTimeUtils.parseDate(startDate);
        LocalDate end = dateTimeUtils.parseDate(endDate);
        List<Object[]> rawResult = wayfairAdsReportDayRepository.getAggregatedReportsByDateRange(start, end);
        for (Object[] result : rawResult) {
            String category = (result[14] != null) ? result[14].toString() : "";
            WayfairAdsReportDto wayfairAdsReportDto = WayfairAdsReportDto.builder()
                    .campaignId(result[0].toString())
                    .parentSku(result[1].toString())
                    .clicks(Long.parseLong(result[2].toString()))
                    .impressions(Long.parseLong(result[3].toString()))
                    .spend(Double.parseDouble(result[4].toString()))
                    .totalSale(Double.parseDouble(result[5].toString()))
                    .totalOrders(Long.parseLong(result[6].toString()))
                    .defaultBid(Double.parseDouble(result[7].toString()))
                    .campaignName(result[8].toString())
                    .parentSkuName(result[9].toString())
                    .products(result[10].toString())
                    .className(result[11].toString())
                    .startDate(result[12].toString())
                    .dailyCap(result[13].toString())
                    .category(category)
                    .build();
            wayfairAdsReportDtos.add(wayfairAdsReportDto);
        }

        return wayfairAdsReportDtos;
    }

    @Override
    public List<WayfairKeywordReportDto> sumClicksByDateRangeKeyword(String startDate, String endDate) {
        List<WayfairKeywordReportDto> wayfairKeywordReportDtos = new ArrayList<>();
        LocalDate start = dateTimeUtils.parseDate(startDate);
        LocalDate end = dateTimeUtils.parseDate(endDate);
        List<Object[]> rawResult = wayfairKeywordReportDailyRepository.getAggregatedReportsByDateRange(start, end);
        for (Object[] result : rawResult) {
            WayfairKeywordReportDto wayfairKeywordReportDto = WayfairKeywordReportDto.builder()
                    .campaignId(result[0].toString())
                    .keywordId(result[1].toString())
                    .keywordValue(result[2].toString())
                    .type(result[3].toString())
                    .clicks(Long.parseLong(result[4].toString()))
                    .impressions(Long.parseLong(result[5].toString()))
                    .spend(Double.parseDouble(result[6].toString()))
                    .totalSale(Double.parseDouble(result[7].toString()))
                    .totalOrders(Long.parseLong(result[8].toString()))
                    .defaultBid(Double.parseDouble(result[9].toString()))
                    .campaignName(result[10].toString())
                    .startDate(result[11].toString())
                    .dailyCap(result[12].toString())
                    .build();
            wayfairKeywordReportDtos.add(wayfairKeywordReportDto);
        }

        return wayfairKeywordReportDtos;
    }

    @Override
    public LocalDate getLastUpdateDate() {
        return wayfairAdsReportDayRepository.findNewestReportDate();
    }

    @Override
    public void updateCategoryCampaign(List<WayfairCampaignCategoryDto> wayfairCampaignCategoryDto) {
        WayfairCampaign wayfairCampaign;
        WayfairCategory wayfairCategory;
        for (WayfairCampaignCategoryDto dto : wayfairCampaignCategoryDto) {
            Optional<WayfairCategory> optionalWayfairCategory = wayfairCategoryRepository.findByTitle(dto.getCategory());
            if (optionalWayfairCategory.isEmpty()) {
                wayfairCategory = WayfairCategory.builder().title(dto.getCategory()).build();
                wayfairCategoryRepository.save(wayfairCategory);
            } else {
                wayfairCategory = optionalWayfairCategory.get();
            }

            for (String campaignId : dto.getCampaignIds()) {
                Optional<WayfairCampaign> optionalWayfairCampaign = wayfairCampaignRepository.findByCampaignId(campaignId);
                if (optionalWayfairCampaign.isPresent()) {
                    wayfairCampaign = optionalWayfairCampaign.get();
                    wayfairCampaign.setCategory(wayfairCategory);
                    wayfairCampaignRepository.save(wayfairCampaign);
                }
            }
        }
    }

    @Override
    public void updateCategoryReports(List<WayfairCategoryReportRequestDto> wayfairCategoryReportRequestDtos) {
        DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reportDate;
        WayfairCategoryReport wayfairCategoryReport;
        WayfairCategory wayfairCategory;
        for (WayfairCategoryReportRequestDto dto : wayfairCategoryReportRequestDtos) {
            Optional<WayfairCategory> optionalWayfairCategory = wayfairCategoryRepository.findByTitle(dto.getCategory());
            wayfairCategory = optionalWayfairCategory.orElseThrow(() ->
                    new ResourceNotFoundException("Wayfair category with ID " + dto.getCategory() + " not found"));

            if (dto.getReportDate().contains("/")) {
                reportDate = LocalDate.parse(dto.getReportDate(), slashFormatter);
            } else {
                reportDate = LocalDate.parse(dto.getReportDate(), hyphenFormatter);
            }
            Optional<WayfairCategoryReport> optionalWayfairCategoryReport = wayfairCategoryReportRepository.findByCategory_TitleAndReportDate(dto.getCategory(), reportDate);
            wayfairCategoryReport = optionalWayfairCategoryReport.orElseGet(WayfairCategoryReport::new);
            wayfairCategoryReport.setAcos(dto.getAcos());
            wayfairCategoryReport.setReportDate(reportDate);
            wayfairCategoryReport.setCategory(wayfairCategory);
            wayfairCategoryReport.setTacos(dto.getTacos());
            wayfairCategoryReport.setAdSpend(dto.getAdSpend());
            wayfairCategoryReport.setSaleByAds(dto.getSaleByAds());
            wayfairCategoryReport.setTargetAcos(dto.getTargetAcos());
            wayfairCategoryReport.setTotalSales(dto.getTotalSale());
            wayfairCategoryReport.setOrderQuantity(dto.getOrderQuantity());
            wayfairCategoryReportRepository.save(wayfairCategoryReport);
        }
    }

    @Override
    public Map<String, WayfairCategoryReport> getCategoryReportsByDate(String date) {
        DateTimeFormatter slashFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter hyphenFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reportDate;
        if (date.contains("/")) {
            reportDate = LocalDate.parse(date, slashFormatter);
        } else {
            reportDate = LocalDate.parse(date, hyphenFormatter);
        }
        List<WayfairCategoryReport> reports = wayfairCategoryReportRepository.findAllByReportDate(reportDate);
        Map<String, WayfairCategoryReport> wayfairCategoryReportMap = new HashMap<>();
        reports.forEach(report -> wayfairCategoryReportMap.put(report.getCategory().getTitle(), report));
        return wayfairCategoryReportMap;
    }
}
