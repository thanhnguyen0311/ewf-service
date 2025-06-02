package com.danny.ewf_service.payload.request;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class LpnRequestDto {
    private String tagID;
    private String sku;
    private Long quantity;
    private String containerNumber;
    private String bayCode;
    private LocalDate date;
}
