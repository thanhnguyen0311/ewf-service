package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.repository.WayfairCampaignParentSkuRepository;
import com.danny.ewf_service.service.WayfairCampaignService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WayfairCampaignServiceImpl implements WayfairCampaignService {

    @Autowired
    private final WayfairCampaignParentSkuRepository wayfairCampaignParentSkuRepository;

    @Override
    public List<WayfairCampaignParentSku> findAllActiveCampaignsWithParentSkus() {
        return wayfairCampaignParentSkuRepository.findAllByCampaignIsActiveIsTrue();
    }
}
