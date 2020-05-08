package com.geminifile.core.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static com.geminifile.core.CONSTANTS.*;

/*
This thread scans for active ip addresses in the network, then checks for open port @ COMMPORT constant.
If it is open, then it gets added to the ActivePeerGetter.activeIpAddresses.
 */

public class PingerThread implements Runnable {

    // Determines the ip number they will be pinging
    private final int factor;
    private final ArrayList<InetAddress> activeIps;

    private static final int range = 255; // Range until stop
    private static String ipBeginning; // WARNING, HAS TO BE SET FIRST BY SERVICE JAVA

    public PingerThread(int factor) {
        this.factor = factor + 1;
        this.activeIps = new ArrayList<>();
    }

    @Override
    public void run() {
        // This sections test for all the ip address connections.
        int repetition = (range / IPPINGERTHREADS) + 1;
        for (int i = 0; i < repetition; i++) {
            int ipToPing = factor + (i * repetition);
            if (ipToPing > range) { break; } // If ip to check is more than 255, then there is no point.

            // Check if ip is open
            try {
                InetAddress ip = InetAddress.getByName(ipBeginning + ipToPing);
                // If ip is reachable then add to list
                if (ip.isReachable(PINGTIMEOUT)) {
                    activeIps.add(ip);
                }
            } catch (UnknownHostException e) {
                // TODO: LOG FOR ERRORS AND CONTINUE
                e.printStackTrace();
            } catch (IOException e) {
                // TODO: LOG FOR ERRORS
                e.printStackTrace();
            }
        }

        if (activeIps.size() == 0) {
            return; // No active ips, stopping thread.
        }

        // Check for open port @ COMMPORT
        for (InetAddress ip : activeIps) {
            // TODO: Change this by implementing message query stuff.
            try {
                Socket tryOpen = new Socket(ip, COMMPORT);
                ActivePeerGetter.addActiveIp(ip);
                tryOpen.close();
                System.out.println(ip.getHostAddress() + ":" + COMMPORT + " Is open!");
            } catch (IOException e) {
                // TODO: LOG ERROR
                System.out.println(ip.getHostAddress() + ":" + COMMPORT + " Not open deh");
            }
        }

    }

    public static void setIpBeginning(String ipBeginning) {
        PingerThread.ipBeginning = ipBeginning;
    }
}