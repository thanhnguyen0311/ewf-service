package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.ILpnMapper;
import com.danny.ewf_service.entity.BayLocation;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.LpnRepository;
import com.danny.ewf_service.service.LpnService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LpnServiceImpl implements LpnService {

    @Autowired
    private final ILpnMapper lpnMapper;

    @Autowired
    private final ComponentRepository componentRepository;

    @Autowired
    private final BayLocationRepository bayLocationRepository;

    @Autowired
    private final LpnRepository lpnRepository;

    @Override
    public void newLpn(LpnRequestDto lpnRequestDto) {
        LPN lpn;
        lpn = lpnMapper.lpnRequestDtoToLpn(lpnRequestDto);
        Optional<Component> component = componentRepository.findBySku(lpnRequestDto.getSku());
        if (component.isPresent()) lpn.setComponent(component.get());
        else throw new RuntimeException("Product not found");

        Optional<BayLocation> bayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
        if (bayLocation.isPresent()) lpn.setBayLocation(bayLocation.get());
        else throw new RuntimeException("Bay location not found");

        lpn.setStatus("active");

        lpnRepository.save(lpn);
    }

    @Override
    public List<LpnResponseDto> getAllLpn() {
        List<LPN> lpns = lpnRepository.findAllByOrderByCreatedDateDesc();
        return lpnMapper.lpnListToLpnResponseDtoList(lpns);
    }
}
