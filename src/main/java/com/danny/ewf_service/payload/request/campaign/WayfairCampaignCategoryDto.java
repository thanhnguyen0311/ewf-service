package com.danny.ewf_service.payload.request.campaign;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairCampaignCategoryDto {
    private String category;
    private List<String> campaignIds;
}
