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
    private String parentSku;
    private String keywordId;
    private Long clicks;
    private Long impressions;
    private Double spend;
    private Double totalSale;
    private Long totalOrders;
}
