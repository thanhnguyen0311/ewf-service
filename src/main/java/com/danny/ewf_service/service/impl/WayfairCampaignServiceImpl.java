package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.wayfair.WayfairCampaign;
import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignCategoryDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.payload.response.campaign.WayfairKeywordReportDto;
import com.danny.ewf_service.repository.WayfairAdsReportDayRepository;
import com.danny.ewf_service.repository.WayfairCampaignParentSkuRepository;
import com.danny.ewf_service.repository.WayfairCampaignRepository;
import com.danny.ewf_service.repository.WayfairKeywordReportDailyRepository;
import com.danny.ewf_service.service.WayfairCampaignService;
import com.danny.ewf_service.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                    .category(result[14].toString())
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
                    .searchTerm(result[13].toString())
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
        for (WayfairCampaignCategoryDto dto : wayfairCampaignCategoryDto) {
            for (String campaignId : dto.getCampaignIds()) {
                Optional<WayfairCampaign> optionalWayfairCampaign = wayfairCampaignRepository.findByCampaignId(campaignId);
                if (optionalWayfairCampaign.isPresent()) {
                    wayfairCampaign = optionalWayfairCampaign.get();
                    wayfairCampaign.setCategory(dto.getCategory());
                    wayfairCampaignRepository.save(wayfairCampaign);
                }
            }
        }

    }


}
