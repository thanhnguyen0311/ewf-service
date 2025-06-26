package com.danny.ewf_service.payload.response.log;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class LpnLogResponseDto {
    private String user;
    private String tagID;
    private String message;
    private String sku;
    private String method;
    private LocalDateTime logDate;
}
