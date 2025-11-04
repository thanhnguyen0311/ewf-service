package com.danny.ewf_service.controller;

import com.danny.ewf_service.service.SftpService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/amazon-edi/test")
public class AmazonEdiTestController {
    @Autowired
    private SftpService sftpService;

    @GetMapping("/connection")
    public ResponseEntity<String> testBasicConnection() {
        boolean success = sftpService.testBasicConnection();
        return success ?
                ResponseEntity.ok("✅ Basic connection successful") :
                ResponseEntity.status(500).body("❌ Connection failed - check logs");
    }

    @PostMapping("/send")
    public ResponseEntity<String> testSending() {
        boolean success = sftpService.testSendingConnection();
        return success ?
                ResponseEntity.ok("✅ Sending test passed - Click 'Refresh' in AWS console") :
                ResponseEntity.status(500).body("❌ Sending test failed - check logs");
    }

    @PostMapping("/receive")
    public ResponseEntity<String> testReceiving() {
        boolean success = sftpService.testReceivingConnection();
        return success ?
                ResponseEntity.ok("✅ Receiving test passed") :
                ResponseEntity.status(500).body("❌ No test files found - click 'Receive test file from Amazon' first");
    }
}
