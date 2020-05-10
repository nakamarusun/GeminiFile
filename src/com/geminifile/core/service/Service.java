package com.geminifile.core.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static com.geminifile.core.CONSTANTS.*;

public class Service {

    public static void start() {

        // Checks the status of network. Is the device connected to any network ?
        InetAddress id;
        try {
            id = InetAddress.getLocalHost();
            String ip = id.getHostAddress();
            if (ip.equals("127.0.0.1")) {
                System.out.println("System is not connected to any network !");
                System.exit(0);
            }
            // SETS THE IP BEGINNING FOR PINGER THREAD
            PingerThread.setIpBeginning(ip.substring(0, ip.lastIndexOf('.') + 1));
        } catch(UnknownHostException e) {
            System.out.println("System cannot resolve a valid address !");
            System.exit(-1);
        }


        // Start pinger to ping all the ranges of the local ip address
        Thread pinger = new Thread(new ActivePeerGetter());
        pinger.setDaemon(true);
        pinger.start();


    }

}