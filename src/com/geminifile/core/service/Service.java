package com.geminifile.core.service;

import com.geminifile.core.service.localhostconn.LocalServerCommunicator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static com.geminifile.core.CONSTANTS.*;

public class Service {

    private static Thread pinger;

    public static void start() {

        // Starts local server msg command processor
        LocalServerCommunicator.startLocalServer();


        // Start pinger to ping all the ranges of the local ip address
        pinger = new Thread(new ActivePeerGetter());
        pinger.setDaemon(true);
        pinger.start();

    }

    public static void restartPinger() {
        pinger.interrupt();
    }

}