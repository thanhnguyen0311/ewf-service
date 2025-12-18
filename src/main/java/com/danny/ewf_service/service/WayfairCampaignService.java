package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;

import java.util.HashMap;
import java.util.List;

public interface WayfairCampaignService {

    List<WayfairCampaignParentSku> findAllActiveCampaignsWithParentSkus();
}
