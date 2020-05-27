package com.geminifile.core.service.localnetworkconn;

/*
Ip change checker for PeerCommunicatorManager.
 */

import com.geminifile.core.service.Service;

import java.net.SocketException;

public class IpChangeChecker implements Runnable {

    Thread threadToInterrupt;

    public IpChangeChecker(Thread threadToInterrupt) {
        this.threadToInterrupt = threadToInterrupt;
    }

    @Override
    public void run() {
        try {
            // If ip registered in Service is not the same as the current detected ip
            if (!Service.getCurrentIp().getHostAddress().equals(Service.getNonLoopbackIp4Address().getHostAddress())) {
                // Interrupt main service thread and restart.
                threadToInterrupt.interrupt();
            }
        } catch (SocketException ignored) { }
    }
}
