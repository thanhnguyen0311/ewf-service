package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignCategoryDto;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignClickRequestDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.payload.response.campaign.WayfairKeywordReportDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public interface WayfairCampaignService {

    List<WayfairCampaignParentSku> findAllActiveCampaignsWithParentSkus();

    List<WayfairAdsReportDto> sumClicksByDateRangeAndParentSkuAndCampaignId(String startDate , String endDate);

    List<WayfairKeywordReportDto> sumClicksByDateRangeKeyword(String startDate , String endDate);
    LocalDate getLastUpdateDate();

    void updateCategoryCampaign(List<WayfairCampaignCategoryDto> wayfairCampaignCategoryDto);
}
