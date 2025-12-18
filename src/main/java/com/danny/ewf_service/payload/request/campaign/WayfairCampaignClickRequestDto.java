package com.danny.ewf_service.payload.request.campaign;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairCampaignClickRequestDto {
    String campaignId;
    String sku;
    String startDate;
    String endDate;
}
