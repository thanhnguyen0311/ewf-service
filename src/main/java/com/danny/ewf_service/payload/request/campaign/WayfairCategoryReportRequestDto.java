package com.danny.ewf_service.payload.request.campaign;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairCategoryReportRequestDto {
    private String reportDate;
    private String category;
    private String impressions;
    private String clicks;
    private String adSpend;
    private String saleByAds;
    private String orderQuantity;
    private String acos;
    private String targetAcos;
}
