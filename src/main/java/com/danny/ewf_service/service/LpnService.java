package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;

import java.util.List;

public interface LpnService {

    void newLpn(LpnRequestDto lpnRequestDto);

    List<LpnResponseDto> getAllLpn();
}
