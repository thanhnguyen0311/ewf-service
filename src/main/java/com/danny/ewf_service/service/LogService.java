package com.danny.ewf_service.service;

import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.entity.LpnLog;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.response.log.LpnLogResponseDto;

import java.util.List;

public interface LogService {

    void createLpnLog (
            LPN lpn,
            String method,
            String previousBay,
            String newBay,
            Long newQuantity,
            Long previousQuantity,
            String previousStatus,
            String newStatus,
            String remarks,
            User user
    );

    List<LpnLogResponseDto> findAllLpnLogs (int page);
}
