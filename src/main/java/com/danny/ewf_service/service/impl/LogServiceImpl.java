package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.entity.LpnLog;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.payload.response.log.LpnLogResponseDto;
import com.danny.ewf_service.repository.LpnLogRepository;
import com.danny.ewf_service.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LogServiceImpl implements LogService {

    @Autowired
    private final LpnLogRepository lpnLogRepository;

    @Override
    public void createLpnLog(LPN lpn, String method, String previousBay, String newBay, Long newQuantity, Long previousQuantity, String previousStatus, String newStatus, String remarks, User user) {
        LpnLog lpnLog = LpnLog.builder()
                .lpn(lpn)
                .method(method)
                .previousBayLocation(previousBay)
                .newBayLocation(newBay)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .logDate(LocalDateTime.now())
                .remarks(remarks)
                .user(user)
                .build();
        lpnLogRepository.save(lpnLog);
    }

    @Override
    public List<LpnLogResponseDto> findAllLpnLogs(int page) {
        PageRequest pageable = PageRequest.of(page, 30, Sort.by("logDate").descending());

        Page<Long> lpnLogIdPage = lpnLogRepository.findAllLpnLogIds(pageable);
        List<Long> lpnLogIds = lpnLogIdPage.getContent();

        if (lpnLogIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<LpnLog> lpnLogs = lpnLogRepository.findAllByOrderByLogDateDesc(lpnLogIds);

        return lpnLogs.parallelStream()
                .map(lpnLog -> {

                    LpnLogResponseDto lpnLogResponseDto = new LpnLogResponseDto();
                    lpnLogResponseDto.setUser(lpnLog.getUser().getFirstName() + " " + lpnLog.getUser().getLastName());
                    lpnLogResponseDto.setTagID(lpnLog.getLpn().getTagID());
                    lpnLogResponseDto.setSku(lpnLog.getLpn().getComponent().getSku());
                    lpnLogResponseDto.setMethod(lpnLog.getMethod());
                    StringBuilder message = new StringBuilder("Edited pallet SKU " + lpnLog.getLpn().getComponent().getSku());

                    // Check if quantity was changed
                    if (lpnLog.getPreviousQuantity() != null && lpnLog.getNewQuantity() != null
                        && !lpnLog.getPreviousQuantity().equals(lpnLog.getNewQuantity())) {
                        message.append(" - Quantity: ").append(lpnLog.getPreviousQuantity())
                                .append(" → ").append(lpnLog.getNewQuantity());
                    }

                    // Check if bay location was changed
                    if (lpnLog.getPreviousBayLocation() != null && lpnLog.getNewBayLocation() != null
                        && !lpnLog.getPreviousBayLocation().equals(lpnLog.getNewBayLocation())) {
                        message.append(" - Location: ").append(lpnLog.getPreviousBayLocation())
                                .append(" → ").append(lpnLog.getNewBayLocation());
                    }

                    // Check if status was changed
                    if (lpnLog.getPreviousStatus() != null && lpnLog.getNewStatus() != null
                        && !lpnLog.getPreviousStatus().equals(lpnLog.getNewStatus())) {
                        message.append(" - Status: ").append(lpnLog.getPreviousStatus())
                                .append(" → ").append(lpnLog.getNewStatus());
                    }

                    // Add remarks if available
                    if (lpnLog.getRemarks() != null && !lpnLog.getRemarks().isEmpty()) {
                        message.append(" - Note: ").append(lpnLog.getRemarks());
                    }

                    lpnLogResponseDto.setMessage(message.toString());
                    lpnLogResponseDto.setLogDate(lpnLog.getLogDate());
                    return lpnLogResponseDto;
                })

                .collect(Collectors.toList());
    }
}
