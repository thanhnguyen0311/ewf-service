package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.converter.ILpnMapper;
import com.danny.ewf_service.entity.*;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.exception.ValidationException;
import com.danny.ewf_service.payload.request.CountingBySkuRequestDto;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import java.util.*;

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

        if (lpnRequestDto.getTagID().length() < 12) throw new ValidationException("tagID", "Tag ID is not valid.");

        if (lpnRepository.existsLPNByTagID(lpnRequestDto.getTagID())) {
            throw new ValidationException("tagID", "Lpn with tag ID " + lpnRequestDto.getTagID() + " already exists.");
        }

        Component component;
        lpn = lpnMapper.lpnRequestDtoToLpn(lpnRequestDto);
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
            lpn.setComponent(component);
        } else throw new ValidationException("sku", "Component with SKU " + lpnRequestDto.getSku() + " not found.");


        if (!lpnRequestDto.getBayCode().isEmpty()) {
            Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
            if (optionalBayLocation.isPresent()) {
                BayLocation bayLocation = optionalBayLocation.get();
                bayLocation.setDefaultSku(lpnRequestDto.getSku());
                bayLocationRepository.save(bayLocation);
                lpn.setBayLocation(bayLocation);
            } else
                throw new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");
        }



        lpn.setStatus("active");
        lpn.setIsDeleted(false);
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
            if (lpn.getBayLocation() != null) previousBay = lpn.getBayLocation();
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

        if (!lpnRequestDto.getBayCode().isEmpty()) {
            if (optionalBayLocation.isPresent()) {
                bayLocation = optionalBayLocation.get();
                lpn.setBayLocation(bayLocation);
            } else throw new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");
        }


        lpnRepository.save(lpn);

        User user = customUserDetailsService.getUser();
        logService.createLpnLog(
                lpn,
                "EDIT",
                previousBay != null ? previousBay.getBayCode() : "",
                lpnRequestDto.getBayCode(),
                previousQuantity,
                lpnRequestDto.getQuantity(),
                previousStatus,
                lpnRequestDto.getStatus(),
                !lpnRequestDto.getContainerNumber().isEmpty() ? "Container : " + lpnRequestDto.getContainerNumber() : "",
                user
        );
    }

    @Override
    public void putAway(LpnEditRequestDto lpnRequestDto) {
        LPN lpn;
        BayLocation bayLocation;
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(lpnRequestDto.getTagID());
        Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
        BayLocation previousBay = null;
        if (optionalLpn.isPresent()) {
            lpn = optionalLpn.get();
            if (lpn.getBayLocation() != null) previousBay = lpn.getBayLocation();
        } else throw new ValidationException("lpn", "LPN with tag ID " + lpnRequestDto.getTagID() + " not found");

        if (optionalBayLocation.isPresent()) {
            bayLocation = optionalBayLocation.get();
            lpn.setBayLocation(bayLocation);
        } else throw new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");

        lpnRepository.save(lpn);

        User user = customUserDetailsService.getUser();
        logService.createLpnLog(
                lpn,
                "PUTAWAY",
                previousBay != null ? previousBay.getBayCode() : "",
                lpnRequestDto.getBayCode(),
                lpn.getQuantity(),
                lpnRequestDto.getQuantity(),
                lpn.getStatus(),
                lpnRequestDto.getStatus(),
                "",
                user
        );
    }

    @Override
    public void breakDown(LpnEditRequestDto lpnRequestDto) {
        LPN lpn;
        Component component;
        BayLocation bayLocation;
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(lpnRequestDto.getTagID());
        Optional<Component> optionalComponent = componentRepository.findBySku(lpnRequestDto.getSku());
        Optional<BayLocation> optionalBayLocation = bayLocationRepository.findByBayCode(lpnRequestDto.getBayCode());
        String previousStatus;

        if (optionalLpn.isPresent()) {
            lpn = optionalLpn.get();
            previousStatus = lpn.getStatus();
            lpn.setStatus(lpnRequestDto.getStatus());
        } else throw new ValidationException("lpn", "LPN with tag ID " + lpnRequestDto.getTagID() + " not found");

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else throw new ValidationException("sku", "Component with SKU " + lpnRequestDto.getSku() + " not found.");

        if (optionalBayLocation.isPresent()) {
            bayLocation = optionalBayLocation.get();
        } else throw new ValidationException("bayCode", "Bay location with code " + lpnRequestDto.getBayCode() + " not found.");

        lpnRepository.save(lpn);

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
                lpn,
                "BREAKDOWN",
                bayLocation.getBayCode(),
                bayLocation.getBayCode(),
                lpn.getQuantity(),
                lpnRequestDto.getQuantity(),
                previousStatus,
                lpnRequestDto.getStatus(),
                "",
                user
        );
    }

    @Override
    public void delete(String tagID) {
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(tagID);
        if (optionalLpn.isPresent()) {
            LPN lpn = optionalLpn.get();
            lpn.setIsDeleted(true);
            lpnRepository.save(lpn);
            String bayCode = lpn.getBayLocation() != null ? lpn.getBayLocation().getBayCode() : "";

            User user = customUserDetailsService.getUser();
            logService.createLpnLog(
                    lpn,
                    "DELETE",
                    bayCode,
                    "",
                    lpn.getQuantity(),
                    lpn.getQuantity(),
                    lpn.getStatus(),
                    "",
                    "pallet deleted",
                    user
            );
        } else throw new ValidationException("lpn", "LPN with tag ID " + tagID + " not found");
    }

    @Override
    public List<LpnResponseDto> getAllLpn() {
        List<LPN> lpns = lpnRepository.findAllByOrderByUpdatedAtDesc();
        return lpnMapper.lpnListToLpnResponseDtoList(lpns);
    }

    @Override
    public LpnResponseDto getLpnById(String tagID) {
        Optional<LPN> optionalLpn = lpnRepository.findByTagID(tagID);
        if (optionalLpn.isPresent()) {
            LPN lpn = optionalLpn.get();
            return lpnMapper.lpnToLpnResponseDto(lpn);
        } else throw new ValidationException("lpn", "LPN with tag ID " + tagID + " not found");
    }

    @Override
    public void transferLpn() {
        String dbHost = "104.236.97.50";       // Example: "104.236.97.51"
        String dbUser = "nct031194";       // Example: "another-user"
        String dbPassword = "Thanh@123";        // Example: "Another@123"
        String dbName = "wms";       // Example: "wms_backup"
        // JDBC URL for the database
        String url = "jdbc:mysql://" + dbHost + "/" + dbName;
        String insertQuery = "INSERT INTO lpnmaster (assigned_location, epc, quantity, sku, status, upc) VALUES ( ?, ?, ?, ?, ?,?)";

        List<LPN> lpns = lpnRepository.findAllByOrderByUpdatedAtDesc();
        for (LPN lpn : lpns) {
            // Your insert query
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                String baycode = "";
                if (lpn.getBayLocation() != null) baycode = lpn.getBayLocation().getBayCode();
                // Setting the values for the placeholders
                preparedStatement.setString(1, baycode);
                preparedStatement.setString(2, lpn.getTagID());
                preparedStatement.setInt(3, Math.toIntExact(lpn.getQuantity()));
                preparedStatement.setString(4, lpn.getComponent().getSku());
                preparedStatement.setString(5, "Active");
                preparedStatement.setString(6, lpn.getComponent().getUpc());

                // Execute the insert
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Insert successful! " + lpn.getTagID());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
}

