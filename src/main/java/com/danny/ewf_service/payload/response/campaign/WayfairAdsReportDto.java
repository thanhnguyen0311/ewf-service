package com.danny.ewf_service.payload.response.campaign;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairAdsReportDto {
    private String campaignId;
    private String parentSku;
    private Long clicks;
    private Long impressions;
    private Double spend;
    private Double totalSale;
    private Long totalOrders;
    private Double defaultBid;
    private String campaignName;
    private String parentSkuName;
    private String products;
    private String className;
}
