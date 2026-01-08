package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignCategoryDto;
import com.danny.ewf_service.payload.request.user.UserCreateRequestDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.payload.response.campaign.WayfairKeywordReportDto;
import com.danny.ewf_service.service.WayfairCampaignService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/campaigns/reportByDate")
    public ResponseEntity<?> getClickStats(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {

        try {
           List<WayfairAdsReportDto> wayfairAdsReportDtos = wayfairCampaignService.sumClicksByDateRangeAndParentSkuAndCampaignId(startDateStr, endDateStr);
            return ResponseEntity.ok(wayfairAdsReportDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving click statistics: " + e.getMessage());
        }
    }

    @GetMapping("/campaigns/keyword/reportByDate")
    public ResponseEntity<?> getKeywordReportsByDate(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {

        try {
            List<WayfairKeywordReportDto> wayfairKeywordReportDtos = wayfairCampaignService.sumClicksByDateRangeKeyword(startDateStr, endDateStr);
            return ResponseEntity.ok(wayfairKeywordReportDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving click statistics: " + e.getMessage());
        }
    }
    @GetMapping("/campaigns/last-update")
    public ResponseEntity<?> getClickStats() {
        try {
            return ResponseEntity.ok(wayfairCampaignService.getLastUpdateDate());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving click statistics: " + e.getMessage());
        }
    }


    @PostMapping("/campaigns/category")
    public ResponseEntity<?> updateCategory(@RequestBody WayfairCampaignCategoryDto wayfairCampaignCategoryDto) {
        try {
            wayfairCampaignService.updateCategoryCampaign(wayfairCampaignCategoryDto);
            return ResponseEntity.status(200).body("Category updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }
}
