package com.danny.ewf_service.service.impl;


import com.danny.ewf_service.converter.IBayLocationMapper;
import com.danny.ewf_service.entity.BayLocation;
import com.danny.ewf_service.payload.response.BayLocationResponseDto;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.service.BayLocationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<BayLocation> bayLocations = bayLocationRepository.findAllByIsActiveTrue();
        return bayLocationMapper.bayLocationToBayLocationResponseDtos(bayLocations);
    }
}
