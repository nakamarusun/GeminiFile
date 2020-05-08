package com.geminifile.core.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import static com.geminifile.core.CONSTANTS.*;

public class Service {

    private static Thread mainThread;

    public static void start() {
        // Assigns the main thread to variable for easy access.
        mainThread = Thread.currentThread();
        // Checks the status of network. Is the device connected to any network ?
        InetAddress id;

        try {
            id = InetAddress.getLocalHost();
            String ip = id.getHostAddress();
            if (ip.equals("127.0.0.1")) {
                System.out.println("System is not connected to any network !");
                System.exit(-1);
            }
            // SETS THE IP BEGINNING FOR PINGER THREAD
            PingerThread.setIpBeginning(ip.substring(0, ip.lastIndexOf('.') + 1));
        } catch(UnknownHostException e) {
            System.out.println("System cannot resolve a valid address !");
            System.exit(-1);
        }

        // Beginning of server listener

        // End of server listener

        Thread pinger = new Thread(new ActivePeerGetter());

        pinger.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // when the pinger is done pinging, do something
        }


    }

    public static Thread getMainThreadRef() {
        return mainThread;
    }

}