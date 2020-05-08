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
            if (id.getHostAddress().equals("127.0.0.1")) {
                System.out.println("System is not connected to any network !");
                System.exit(-1);
            }
        } catch(UnknownHostException e) {
            System.out.println("System cannot resolve a valid address !");
            System.exit(-1);
        }

        Thread pinger = new Thread(new ActivePeerGetter());

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