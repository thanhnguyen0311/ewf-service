package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.entity.LPN;
import com.danny.ewf_service.entity.LpnLog;
import com.danny.ewf_service.entity.auth.User;
import com.danny.ewf_service.repository.LpnLogRepository;
import com.danny.ewf_service.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
