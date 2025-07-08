package com.danny.ewf_service.payload.request;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class LpnEditRequestDto {
    private String tagID;
    private String sku;
    private Long quantity;
    private String containerNumber;
    private String bayCode;
    private String status;

}
