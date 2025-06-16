package com.danny.ewf_service.service.impl;


import com.danny.ewf_service.converter.IBayLocationMapper;
import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.service.BayLocationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BayLocationServiceImpl implements BayLocationService {

    @Autowired
    private final BayLocationRepository bayLocationRepository;

    @Autowired
    private final IBayLocationMapper bayLocationMapper;

    @Override
    public List<BayLocationResponseDto> findAll() {

        List<Object[]> results = bayLocationRepository.findAllBayByIsActiveTrue();
        List<BayLocationResponseDto> bayLocationResponseDtos = new ArrayList<>();

        for (Object[] row : results) {
            String bayCode = (String) row[0];
            String defaultSku = (String) row[1];
            String zone = (String) row[2];
            Long maxPallets = ((Number) row[3]).longValue();
            Long capacity = ((Number) row[4]).longValue();
            Long availableSpace = ((Number) row[5]).longValue();

            bayLocationResponseDtos.add(new BayLocationResponseDto(bayCode,defaultSku , zone, maxPallets, capacity, availableSpace));
        }

        return bayLocationResponseDtos;
    }
}
