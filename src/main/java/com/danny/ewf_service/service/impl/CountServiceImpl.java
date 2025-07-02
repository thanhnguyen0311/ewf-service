package com.danny.ewf_service.service.impl;


import com.danny.ewf_service.converter.ILpnMapper;
import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.payload.request.CountingBySkuRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.repository.LpnRepository;
import com.danny.ewf_service.service.CountService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CountServiceImpl implements CountService {

    @Autowired
    private final LpnRepository lpnRepository;

    @Autowired
    private final ILpnMapper lpnMapper;

    @Override
    public List<LpnResponseDto> findLpnByListTagID(CountingBySkuRequestDto countingBySkuRequestDto) {
        if (countingBySkuRequestDto.getTagIDs() == null || countingBySkuRequestDto.getTagIDs().trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Split the comma-separated string into a list
        List<String> tagIDList = Arrays.stream(countingBySkuRequestDto.getTagIDs().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (tagIDList.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch LPNs with the provided tagIDs
        List<LPN> lpns = lpnRepository.findByTagIDsOrderByUpdatedAtDesc(tagIDList, countingBySkuRequestDto.getSku());

        return lpnMapper.lpnListToLpnResponseDtoList(lpns);
    }
}
