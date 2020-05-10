package com.geminifile.core.service;

import java.net.InetAddress;
import java.util.Set;
import java.util.Vector;

import static com.geminifile.core.CONSTANTS.*;

/* This service runs every X seconds, to get all the concurrently running geminifile services
in the network.
 */

public class ActivePeerGetter implements Runnable {
    // Vector is used here because of its' safety feature for multithreading workloads.
    private static Vector<InetAddress> activeIpAddresses;
    private final Thread threadToInterrupt;

    public ActivePeerGetter(Thread threadToInterrupt) {
        activeIpAddresses = new Vector<>();
        this.threadToInterrupt = threadToInterrupt;
    }

    @Override
    public void run() {

        // This section pings and collects active ip addresses
        ThreadGroup pinger = new ThreadGroup("Pinger");
        // Creates pinger threads.
        for (int i = 0; i < IPPINGERTHREADS; i++) {
            new Thread(pinger, new PingerThread(i)).start();
        }

        // Wait until thread group is dead
        while(true) {
            System.out.println(pinger.activeCount());
            if (pinger.activeCount() == 0) {
                break;
            }
            // So that the process does not run each tick, pause for a little while
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        pinger.destroy();

        // End the thread by interrupting the main one.
        threadToInterrupt.interrupt();
    }

    public static void addActiveIp(InetAddress ip) {
        activeIpAddresses.add(ip);
    }
}