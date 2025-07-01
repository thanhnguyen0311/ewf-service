package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.LpnEditRequestDto;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;

import java.util.List;

public interface LpnService {

    void newLpn(LpnRequestDto lpnRequestDto);

    void updateLpn(LpnEditRequestDto lpnRequestDto);

    void putAway(LpnEditRequestDto lpnRequestDto);

    void breakDown(LpnEditRequestDto lpnRequestDto);

    void delete(String tagID);

    List<LpnResponseDto> getAllLpn();

    LpnResponseDto getLpnById(String tagID);
}
