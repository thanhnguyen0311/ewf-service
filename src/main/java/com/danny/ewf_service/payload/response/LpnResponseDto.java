package com.danny.ewf_service.payload.response;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class LpnResponseDto {
    private String tagID;
    private String sku;
    private Long quantity;
    private String containerNumber;
    private String bayCode;
    private String zone;
    private String status;
    private LocalDate date;
}
