package com.danny.ewf_service.service.impl;

import com.danny.ewf_service.service.SftpService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;

@Service
@AllArgsConstructor
public class SftpServiceImpl implements SftpService {

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.username}")
    private String sftpUsername;

    @Value("${sftp.private.key.path}")
    private String privateKeyPath;

    @Value("${sftp.remote.directory}")
    private String remoteDirectory;

    @Value("${sftp.local.directory}")
    private String localDirectory;

    @Override
    public Session createSession() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath);

            Session session = jsch.getSession(sftpUsername, sftpHost, sftpPort);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(30000); // 30 second timeout
            return session;
        } catch (JSchException e) {
            throw new RuntimeException("Failed to create SFTP session", e);
        }
    }

    @Override
    public ChannelSftp createChannel(Session session) {
        try {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(30000);
            return channelSftp;
        } catch (JSchException e) {
            throw new RuntimeException("Failed to create SFTP channel", e);
        }
    }

    // Test Sending - Upload ConnectivityTest file
    @Override
    public boolean testSendingConnection() {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            System.out.println("🔄 Testing SFTP Sending Connection...");
            System.out.println("Host: " + sftpHost + ":" + sftpPort);
            System.out.println("Username: " + sftpUsername);

            // Create session
            session = createSession();
            System.out.println("✓ Session created successfully");

            // Create SFTP channel
            channelSftp = createChannel(session);
            System.out.println("✓ SFTP channel connected");

            // Create test file
            String testFileName = "ConnectivityTest";
            String testContent = "Test file for Amazon EDI SFTP connection - " + new Date();

            // Ensure local directory exists
            Files.createDirectories(Paths.get(localDirectory));

            // Write test file locally
            Path testFilePath = Paths.get(localDirectory, testFileName);
            Files.write(testFilePath, testContent.getBytes());

            // Upload to Amazon
            // Change to upload directory (usually root or 'upload')
            try {
                channelSftp.cd("/"); // or channelSftp.cd("upload");
            } catch (SftpException e) {
                System.out.println("⚠ Could not change to root directory, using current");
            }

            channelSftp.put(testFilePath.toString(), testFileName);
            System.out.println("✓ File uploaded successfully: " + testFileName);

            // Verify upload
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls("*");
            boolean fileExists = files.stream()
                    .anyMatch(entry -> entry.getFilename().equals(testFileName));

            if (fileExists) {
                System.out.println("✓ File verified on remote server");
                System.out.println("✅ SENDING TEST PASSED - Now click 'Refresh' in AWS console");
                return true;
            } else {
                System.out.println("✗ File not found on remote server after upload");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Sending test failed: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    // Test Receiving - Check for files from Amazon
    @Override
    public boolean testReceivingConnection() {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            System.out.println("🔄 Testing SFTP Receiving Connection...");

            // Create session
            session = createSession();
            System.out.println("✓ Session created successfully");

            // Create SFTP channel
            channelSftp = createChannel(session);
            System.out.println("✓ SFTP channel connected");

            // Change to download directory
            channelSftp.cd(remoteDirectory);
            System.out.println("✓ Changed to directory: " + remoteDirectory);

            // List files
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls("*");
            System.out.println("📂 Found " + files.size() + " files/entries");

            boolean testFileFound = false;
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getAttrs().isDir() && !entry.getFilename().startsWith(".")) {
                    System.out.println("  - " + entry.getFilename() +
                                       " (" + entry.getAttrs().getSize() + " bytes)");

                    // Check if it's a test file from Amazon
                    if (entry.getFilename().toLowerCase().contains("test")) {
                        testFileFound = true;

                        // Download the file
                        Files.createDirectories(Paths.get(localDirectory));
                        String localFilePath = Paths.get(localDirectory, entry.getFilename()).toString();
                        channelSftp.get(entry.getFilename(), localFilePath);
                        System.out.println("✓ Downloaded: " + entry.getFilename());

                        // Delete from remote as instructed by AWS
                        channelSftp.rm(entry.getFilename());
                        System.out.println("✓ Deleted from remote: " + entry.getFilename());
                    }
                }
            }

            if (testFileFound) {
                System.out.println("✅ RECEIVING TEST PASSED");
                return true;
            } else {
                System.out.println("⚠ No test files found. Make sure to click 'Receive test file from Amazon' first");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Receiving test failed: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    // Test basic connection
    @Override
    public boolean testBasicConnection() {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            System.out.println("🔄 Testing Basic SFTP Connection...");
            System.out.println("Host: " + sftpHost + ":" + sftpPort);
            System.out.println("Username: " + sftpUsername);
            System.out.println("Private Key: " + privateKeyPath);

            session = createSession();
            System.out.println("✓ Session connected successfully");

            channelSftp = createChannel(session);
            System.out.println("✓ SFTP channel opened successfully");

            String pwd = channelSftp.pwd();
            System.out.println("✓ Current directory: " + pwd);

            System.out.println("✅ BASIC CONNECTION TEST PASSED");
            return true;

        } catch (Exception e) {
            System.err.println("✗ Connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
