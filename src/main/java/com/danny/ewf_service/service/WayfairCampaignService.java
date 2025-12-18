package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignClickRequestDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;

import java.util.HashMap;
import java.util.List;

public interface WayfairCampaignService {

    List<WayfairCampaignParentSku> findAllActiveCampaignsWithParentSkus();

    List<WayfairAdsReportDto> sumClicksByDateRangeAndParentSkuAndCampaignId(String startDate , String endDate);
}
