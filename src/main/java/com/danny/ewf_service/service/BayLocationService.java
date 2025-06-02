package com.danny.ewf_service.service;

import com.danny.ewf_service.payload.response.BayLocationResponseDto;

import java.util.List;

public interface BayLocationService {

    List<BayLocationResponseDto> findAll();


}
