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
    private Double adSpend;
    private Double saleByAds;
    private Integer orderQuantity;
    private Double acos;
    private Double targetAcos;
}
