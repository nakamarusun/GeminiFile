package com.geminifile.core.service;

import java.util.Vector;

import static com.geminifile.core.CONSTANTS.*;

/* This service runs every X seconds, to get all the concurrently running geminifile services
in the network.
 */

public class ActivePeerGetter implements Runnable {
    private static Vector<String> activeIpAddresses;

    @Override
    public void run() {

        // This section pings and collects active ip addresses
        ThreadGroup pinger = new ThreadGroup("Pinger");

        for (int i = 0; i < IPPINGERTHREADS; i++) {
            new Thread(pinger, new PingerThread(i));
        }



        // Wait until thread group is dead



        // End the thread by interrupting the main one.
        Service.getMainThreadRef().interrupt();
    }
}