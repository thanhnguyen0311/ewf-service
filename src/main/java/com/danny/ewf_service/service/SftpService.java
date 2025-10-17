package com.danny.ewf_service.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public interface SftpService {
    Session createSession();
    ChannelSftp createChannel(Session session);

    boolean testSendingConnection();
    boolean testReceivingConnection();
    boolean testBasicConnection();

}
