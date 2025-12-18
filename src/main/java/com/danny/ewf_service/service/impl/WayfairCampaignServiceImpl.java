package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.repository.WayfairAdsReportDayRepository;
import com.danny.ewf_service.repository.WayfairCampaignParentSkuRepository;
import com.danny.ewf_service.service.WayfairCampaignService;
import com.danny.ewf_service.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return wayfairCampaignParentSkuRepository.findAllByCampaignIsActiveIsTrue();
    }

    @Override
    public Long sumClicksByDateRangeAndParentSkuAndCampaignId(String startDate , String endDate, String parentSku, String campaignId) {
        return wayfairAdsReportDayRepository.sumClicksByDateRangeAndParentSkuAndCampaignId(
                dateTimeUtils.parseDate(startDate),
                dateTimeUtils.parseDate(endDate),
                parentSku,
                campaignId
        );
    }
}
