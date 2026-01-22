package com.danny.ewf_service.controller;


import com.danny.ewf_service.entity.wayfair.WayfairCampaignParentSku;
import com.danny.ewf_service.entity.wayfair.WayfairCategoryReport;
import com.danny.ewf_service.payload.request.campaign.WayfairBiddingLogicRequestDto;
import com.danny.ewf_service.payload.request.campaign.WayfairCampaignCategoryDto;
import com.danny.ewf_service.payload.request.campaign.WayfairCategoryReportRequestDto;
import com.danny.ewf_service.payload.request.user.UserCreateRequestDto;
import com.danny.ewf_service.payload.response.campaign.WayfairAdsReportDto;
import com.danny.ewf_service.payload.response.campaign.WayfairKeywordReportDto;
import com.danny.ewf_service.repository.Wayfair.WayfairKeywordReportDailyRepository;
import com.danny.ewf_service.service.WayfairCampaignService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.GET;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/ad")
@RestController
@AllArgsConstructor
public class WayfairCampaignController {

    @Autowired
    private final WayfairCampaignService wayfairCampaignService;

    @Autowired
    private final WayfairKeywordReportDailyRepository wayfairKeywordReportDailyRepository;

    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllActiveCampaigns() {
        try {
            List<WayfairCampaignParentSku> wayfairCampaignParentSkus = wayfairCampaignService.findAllActiveCampaignsWithParentSkus();
            return ResponseEntity.ok(wayfairCampaignParentSkus);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching campaigns");
        }
    }


    @GetMapping("/campaigns/category/report")
    public ResponseEntity<?> getCategoryReport(@RequestParam String date) {
        try {
            Map<String, WayfairCategoryReport> wayfairCategoryReportMap =  wayfairCampaignService.getCategoryReportsByDate(date);
            return ResponseEntity.status(200).body(wayfairCategoryReportMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("ERROR");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("ERROR");
        }
    }

    @GetMapping("/campaigns/category/report/recent")
    public ResponseEntity<?> getRecentCategoryReport() {
        try {
            Map<LocalDate, Map<String, WayfairCategoryReport>> wayfairCategoryReportMap =  wayfairCampaignService.getRecentCategoryReports();
            return ResponseEntity.status(200).body(wayfairCategoryReportMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("ERROR");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("ERROR");
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
    public ResponseEntity<?> getProductLastUpdate() {
        try {
            System.out.println("Fetching last update date");
            return ResponseEntity.ok(wayfairCampaignService.getLastUpdateDate());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving click statistics: " + e.getMessage());
        }
    }

    @GetMapping("/campaigns/keyword/last-update")
    public ResponseEntity<?> getKeywordLastUpdate() {
        try {
            return ResponseEntity.ok(wayfairKeywordReportDailyRepository.findNewestReportDate());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving click statistics: " + e.getMessage());
        }
    }


    @PostMapping("/campaigns/category")
    public ResponseEntity<?> updateCategory(@RequestBody List<WayfairCampaignCategoryDto> wayfairCampaignCategoryDtos) {
        try {
            wayfairCampaignService.updateCategoryCampaign(wayfairCampaignCategoryDtos);
            return ResponseEntity.status(200).body("Category updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }

    @PostMapping("/campaigns/category/report")
    public ResponseEntity<?> updateCategoryReport(@RequestBody List<WayfairCategoryReportRequestDto> wayfairCategoryReportRequestDtos) {
        try {
            wayfairCampaignService.updateCategoryReports(wayfairCategoryReportRequestDtos);
            return ResponseEntity.status(200).body("Category updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }

    @PostMapping("/bidding")
    public ResponseEntity<?> updateBiddingLogic(@RequestBody WayfairBiddingLogicRequestDto wayfairBiddingLogicRequestDto) {
        try {
            wayfairCampaignService.updateBiddingLogic(wayfairBiddingLogicRequestDto);
            return ResponseEntity.status(200).body("Category updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }

    @GetMapping("/bidding")
    public ResponseEntity<?> getBiddingLogic(@RequestParam String category) {
        try {
            String logic = wayfairCampaignService.getBiddingLogic(category);
            return ResponseEntity.status(200).body(logic);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }

    @GetMapping("/bidding/all")
    public ResponseEntity<?> getAllBiddingLogic() {
        try {
            Map<String, String> logic = wayfairCampaignService.getAllBiddingLogic();
            return ResponseEntity.status(200).body(logic);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Cannot create user");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating new user");
        }
    }
}
