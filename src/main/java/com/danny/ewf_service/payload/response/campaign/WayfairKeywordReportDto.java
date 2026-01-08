package com.danny.ewf_service.payload.response.campaign;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairKeywordReportDto {
    private String campaignId;
    private String keywordId;
    private String keywordValue;
    private String type;
    private Long clicks;
    private Long impressions;
    private Double spend;
    private Double totalSale;
    private Long totalOrders;
    private Double defaultBid;
    private String campaignName;
    private String startDate;
    private String dailyCap;
    private String searchTerm;
}
