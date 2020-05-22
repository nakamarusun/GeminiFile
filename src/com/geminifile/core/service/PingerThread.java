package com.geminifile.core.service;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgIdentification;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

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
//        System.out.println("Starting pinger " + factor);
        // This sections test for all the ip address connections.
        // Checks for active ip addresses, and inserts it into an arrayList
        int repetition = (range / IPPINGERTHREADS) + 1;
        for (int i = 0; i < repetition; i++) {
            // Check whether thread is interrupted, and stops it if it has to.
            if (Thread.currentThread().isInterrupted()) {
//                System.out.println("Pinger thread interrupted at " + factor );
                return;
            }
            int ipToPing = factor + (i * repetition);
            if (ipToPing > range) { break; } // If ip to check is more than 255, then there is no point.

            // Check if ip is open
            try {
                InetAddress ip = InetAddress.getByName(ipBeginning + ipToPing);
                // If ip is reachable then add to list
                if (ip.isReachable(PINGTIMEOUT)) {
                    activeIps.add(ip);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (activeIps.size() == 0) {
            return; // No active ips, stopping thread.
        }

        // Check for open port @ COMMPORT
        for (InetAddress ip : activeIps) {
            try {
                Socket tryOpen = new Socket();
                tryOpen.connect(new InetSocketAddress(ip, COMMPORT), PORTCONNECTTIMEOUT);

                // Do the msg query here
                ObjectOutputStream localObjectOut = new ObjectOutputStream(tryOpen.getOutputStream());
                ObjectInputStream localObjectIn = new ObjectInputStream(tryOpen.getInputStream());

                // Send connection query to the device
                localObjectOut.writeObject(new MsgIdentification("ping", MsgType.CONNQUERY, Service.getMyNode()));
                // See if the reply is good
                try {
                    MsgWrapper reply = (MsgWrapper)localObjectIn.readObject();
                    // if the reply is not expected then throw an IOException error
                    if ( !(reply.getType() == MsgType.CONNACCEPT && reply.getContent().equals("pinggood")) ) {
                        throw new IOException("Reply not expected from peer");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Class deserialization error");
                    e.printStackTrace();
                }
                // Msg query ends

                if (Thread.currentThread().isInterrupted()) {
//                    System.out.println("Pinger thread interrupted at " + factor );
                    return;
                }
                ActivePeerGetter.addActiveTempIp(ip);   // add to temporary vector
                tryOpen.close();
                System.out.println(ip.getHostAddress() + ":" + COMMPORT + " Is open!");
            } catch (IOException e) {
                // If connection is error
                System.out.println(ip.getHostAddress() + ":" + COMMPORT + " Not open deh or timeout");
            }
        }

    }

    public static void setIpBeginning(String ipBeginning) {
        PingerThread.ipBeginning = ipBeginning;
    }
}