package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.repository.WayfairAdsReportDayRepository;
import com.danny.ewf_service.repository.WayfairCampaignParentSkuRepository;
import com.danny.ewf_service.service.WayfairCampaignService;
import com.danny.ewf_service.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class WayfairCampaignServiceImpl implements WayfairCampaignService {

    @Autowired
    private final WayfairCampaignParentSkuRepository wayfairCampaignParentSkuRepository;

    @Autowired
    private final WayfairAdsReportDayRepository wayfairAdsReportDayRepository;

    @Autowired
    private final DateTimeUtils dateTimeUtils;

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
                    .build();
            wayfairAdsReportDtos.add(wayfairAdsReportDto);
        }

        return wayfairAdsReportDtos;
    }
}
