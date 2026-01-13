package com.danny.ewf_service.payload.request.campaign;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class WayfairBiddingLogicRequestDto {

    private String category;
    private JsonNode biddingLogic;
}
