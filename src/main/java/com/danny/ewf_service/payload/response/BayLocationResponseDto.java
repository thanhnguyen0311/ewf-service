package com.danny.ewf_service.payload.response;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
@Data
public class BayLocationResponseDto {
    private String bayCode;
    private String defaultSku;
    private String zone;
    private Long maxPallets;
    private Long capacity;
    private Long availableSpace;
}
