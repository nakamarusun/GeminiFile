package com.geminifile.core.service;

import com.geminifile.core.service.localhostconn.LocalServerCommunicator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static com.geminifile.core.CONSTANTS.*;

public class Service {

    public static void start() {

        // Starts local server msg command processor
        LocalServerCommunicator.startLocalServer();


        // Start pinger to ping all the ranges of the local ip address
        Thread pinger = new Thread(new ActivePeerGetter());
        pinger.setDaemon(true);
        pinger.start();


    }

}