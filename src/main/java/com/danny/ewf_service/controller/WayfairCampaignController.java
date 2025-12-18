package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.response.component.ComponentInventoryResponseDto;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.WayfairCampaignService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/ad")
@RestController
@AllArgsConstructor
public class WayfairCampaignController {

    @Autowired
    private final WayfairCampaignService wayfairCampaignService;

    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllActiveCampaigns() {
        try {
            List<WayfairCampaignParentSku> wayfairCampaignParentSkus = wayfairCampaignService.findAllActiveCampaignsWithParentSkus();
            return ResponseEntity.ok(wayfairCampaignParentSkus);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching product");
        }
    }
}
