package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.request.CountingBySkuRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;

import java.util.List;

public interface CountService {

    List<LpnResponseDto> findLpnByListTagID(CountingBySkuRequestDto countingBySkuRequestDto);


}
