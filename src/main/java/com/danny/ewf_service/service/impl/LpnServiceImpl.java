package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.ILpnMapper;
import com.danny.ewf_service.entity.*;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.exception.ValidationException;
import com.danny.ewf_service.payload.request.LpnEditRequestDto;
import com.danny.ewf_service.payload.request.LpnRequestDto;
import com.danny.ewf_service.payload.response.LpnResponseDto;
import com.danny.ewf_service.repository.BayLocationRepository;
import com.danny.ewf_service.repository.ComponentRepository;
import com.danny.ewf_service.repository.LooseInventoryRepository;
import com.danny.ewf_service.repository.LpnRepository;
import com.danny.ewf_service.service.InventoryService;
import com.danny.ewf_service.service.LogService;
import com.danny.ewf_service.service.LpnService;
import com.danny.ewf_service.service.auth.CustomUserDetailsService;
import jakarta.transaction.Transactional;
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

    @Autowired
    private final InventoryService inventoryService;

    @Autowired
    private LooseInventoryRepository looseInventoryRepository;

    @Override
    public void newLpn(LpnRequestDto lpnRequestDto) {
        LPN lpn;

        if (lpnRepository.existsLPNByTagID(lpnRequestDto.getTagID())) {
            throw new ValidationException("tagID", "Lpn with tag ID " + lpnRequestDto.getTagID() + " already exists.");
        }

        Component component;
        lpn = lpnMapper.lpnRequestDtoToLpn(lpnRequestDto);
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
            lpn.setComponent(component);
        }
        else throw new ValidationException("sku", "Component with SKU " + lpnRequestDto.getSku() + " not found.");


        if (!lpnRequestDto.getBayCode().isEmpty()){
            Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
            if (optionalBayLocation.isPresent()) {
                BayLocation bayLocation = optionalBayLocation.get();
                bayLocation.setDefaultSku(lpnRequestDto.getSku());
                bayLocationRepository.save(bayLocation);
                lpn.setBayLocation(bayLocation);
            }
            else throw new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");
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
    @Transactional
    public void updateLpn(LpnEditRequestDto lpnRequestDto) {
        LPN lpn;
        Component component;
        BayLocation bayLocation;
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(lpnRequestDto.getTagID());
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
        BayLocation previousBay = null;
        long previousQuantity;
        String previousStatus;

        if (optionalLpn.isPresent()) {
            lpn = optionalLpn.get();
            if (lpn.getBayLocation() != null)  previousBay = lpn.getBayLocation();
            previousQuantity = lpn.getQuantity();
            previousStatus = lpn.getStatus();

            lpn.setStatus(lpnRequestDto.getStatus());
            lpn.setContainerNumber(lpnRequestDto.getContainerNumber());
            lpn.setQuantity(lpnRequestDto.getQuantity());
        } else throw new ValidationException("lpn", "LPN with tag ID " + lpnRequestDto.getTagID() + " not found");

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
            lpn.setComponent(component);
        } else throw new ValidationException("sku", "Component with SKU " + lpnRequestDto.getSku() + " not found.");

        if (optionalBayLocation.isPresent()) {
            bayLocation = optionalBayLocation.get();
            lpn.setBayLocation(bayLocation);
        } else throw  new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");

        lpnRepository.save(lpn);

        // breakdown pallet
        if (previousStatus.equals("active") && lpnRequestDto.getStatus().equals("inactive")) {
            LooseInventory looseInventory = inventoryService.findLooseInventoryByLpn(lpn);
            if (looseInventory == null) {
                looseInventory = new LooseInventory();
                looseInventory.setBayLocation(bayLocation);
                looseInventory.setComponent(component);
            }
            looseInventory.setQuantity(looseInventory.getQuantity() + lpnRequestDto.getQuantity());
            looseInventoryRepository.save(looseInventory);
        }


        User user = customUserDetailsService.getUser();
        logService.createLpnLog(
                lpn, "EDIT",
                previousBay != null ? previousBay.getBayCode() : "",
                lpnRequestDto.getBayCode(),
                previousQuantity,
                lpnRequestDto.getQuantity(),
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
