package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.ILpnMapper;
import com.danny.ewf_service.entity.BayLocation;
import com.danny.ewf_service.entity.Component;
import com.danny.ewf_service.entity.Dimension;
import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.request.LpnEditRequestDto;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.LpnRepository;
import com.danny.ewf_service.service.LogService;
import com.danny.ewf_service.service.LpnService;
import com.danny.ewf_service.service.auth.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Objects;
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
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    private final LogService logService;

    @Autowired
    private final LpnRepository lpnRepository;

    @Override
    public void newLpn(LpnRequestDto lpnRequestDto) {
        LPN lpn;

        Component component;
        lpn = lpnMapper.lpnRequestDtoToLpn(lpnRequestDto);
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
            lpn.setComponent(component);
        }
        else throw new RuntimeException("Product not found");

        if (!lpnRequestDto.getBayCode().isEmpty()){
            Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
            if (optionalBayLocation.isPresent()) {
                BayLocation bayLocation = optionalBayLocation.get();
                bayLocation.setDefaultSku(lpnRequestDto.getSku());
                bayLocationRepository.save(bayLocation);
                lpn.setBayLocation(bayLocation);
            }
            else throw new RuntimeException("Bay location not found");
        }

        lpn.setStatus("active");
        lpnRepository.save(lpn);

        Dimension dimension = component.getDimension();
        if (dimension == null) dimension = new Dimension();
        dimension.setPalletCapacity(lpnRequestDto.getQuantity());
        component.setDimension(dimension);
        componentRepository.save(component);

        User user = customUserDetailsService.getUser();
        logService.createLpnLog(
                lpn,
                "CREATE",
                "",
                lpnRequestDto.getBayCode(),
                lpnRequestDto.getQuantity(),
                0L,
                "",
                "active",
                "",
                user);
    }

    @Override
    public void updateLpn(LpnEditRequestDto lpnRequestDto) {
        LPN lpn;
        Component component;
        BayLocation bayLocation;
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(lpnRequestDto.getTagID());
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
        BayLocation previousBay;
        long previousQuantity;
        String previousStatus;

        if (optionalLpn.isPresent()) {
            lpn = optionalLpn.get();
            previousBay = lpn.getBayLocation();
            previousQuantity = lpn.getQuantity();
            previousStatus = lpn.getStatus();

            lpn.setStatus(lpnRequestDto.getStatus());
            lpn.setContainerNumber(lpnRequestDto.getContainerNumber());
            lpn.setQuantity(lpnRequestDto.getQuantity());
        } else throw new RuntimeException("Lpn not found");

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
            lpn.setComponent(component);
        } else throw new RuntimeException("Product not found");

        if (optionalBayLocation.isPresent()) {
            bayLocation = optionalBayLocation.get();
            lpn.setBayLocation(bayLocation);
        } else throw new RuntimeException("Bay location not found");

        lpnRepository.save(lpn);

        User user = customUserDetailsService.getUser();
        logService.createLpnLog(
                lpn, "EDIT",
                previousBay.getBayCode(),
                lpnRequestDto.getBayCode(),
                lpnRequestDto.getQuantity(),
                previousQuantity,
                previousStatus,
                lpnRequestDto.getStatus(),
                "",
                user);
    }

    @Override
    public List<LpnResponseDto> getAllLpn() {
        List<LPN> lpns = lpnRepository.findAllByOrderByCreatedDateDesc();
        return lpnMapper.lpnListToLpnResponseDtoList(lpns);
    }
}
