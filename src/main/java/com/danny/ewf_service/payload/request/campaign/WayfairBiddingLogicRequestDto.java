package com.danny.ewf_service.payload.request.campaign;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairBiddingLogicRequestDto {

    private String category;
    private String biddingLogic;
}
