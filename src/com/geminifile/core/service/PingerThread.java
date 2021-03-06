package com.geminifile.core.service;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgIdentification;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import static com.geminifile.core.CONSTANTS.*;

/*
This thread scans for active ip addresses in the network, then checks for open port @ COMMPORT constant.
If it is open, then it gets added to the ActivePeerGetter.activeIpAddresses.
 */

// TODO: Make this more efficient.
public class PingerThread implements Runnable {

    // Determines the ip number they will be pinging
    private final int factor;

    private static final int range = 255; // Range until stop
    private static String ipBeginning; // WARNING, HAS TO BE SET FIRST BY SERVICE JAVA

    public PingerThread(int factor) {
        this.factor = factor + 1;
    }

    @Override
    public void run() {
//        Service.LOGGER.finer("Starting pinger " + factor);
        Thread.currentThread().setName("Pinger-" + factor);
        // This sections test for all the ip address connections.
        // Checks for active ip addresses, and inserts it into an arrayList
        int repetition = (range / IPPINGERTHREADS) + 1;
        for (int i = 0; i < repetition; i++) {
            // Check whether thread is interrupted, and stops it if it has to.
            if (Thread.currentThread().isInterrupted()) {
//                Service.LOGGER.warning("Pinger thread interrupted at " + factor );
                return;
            }
            int ipToPing = factor + (i * IPPINGERTHREADS);
            if (ipToPing > range) { break; } // If ip to check is more than 255, then there is no point.

            // Check if ip is open
            try {
                InetAddress ip = InetAddress.getByName(ipBeginning + ipToPing);
                // Check to not match self ip
//                if (ip.equals(Service.getCurrentIp())) { continue; } // Code to check self ip.

                // Pings the ip by checking the open port @ COMMPORT and attempting to query with it.
                // Check for open port @ COMMPORT
                try {
                    Socket tryOpen = new Socket();
                    tryOpen.connect(new InetSocketAddress(ip, COMMPORT), PINGTIMEOUT);
//                Service.LOGGER.finer("Connected with ip: " + tryOpen.getInetAddress().getHostAddress());
                    // Do the msg query here
                    ObjectOutputStream localObjectOut = new ObjectOutputStream(tryOpen.getOutputStream());
                    ObjectInputStream localObjectIn = new ObjectInputStream(tryOpen.getInputStream());

                    // Send a connection query to the device with intention to ping
                    localObjectOut.writeObject(new MsgIdentification("ping", MsgType.CONNQUERY, Service.getMyNode()));
//                Service.LOGGER.finer("Sent ping to ip: " + tryOpen.getInetAddress().getHostAddress());
                    // See if the reply is good
                    try {
                        MsgWrapper reply = (MsgWrapper)localObjectIn.readObject();
                        // if the reply is not expected then throw an IOException error
                        if ( !(reply.getType() == MsgType.CONNACCEPT && reply.getContent().equals("pinggood")) ) {
                            throw new IOException("Reply not expected from peer");
                        }
                    } catch (ClassNotFoundException e) {
                        Service.LOGGER.severe("Class deserialization error");
                        Service.LOGGER.log(Level.SEVERE, "exception", e);
                    }
                    // Msg query ends

                    if (Thread.currentThread().isInterrupted()) {
//                    Service.LOGGER.warning("Pinger thread interrupted at " + factor );
                        return;
                    }
                    PingerManager.addActiveTempIp(ip);   // add to temporary vector
                    tryOpen.close();
                    Service.LOGGER.info(ip.getHostAddress() + ":" + COMMPORT + " Is open!");
                } catch (IOException e) {
                    // If connection is error
//                    Service.LOGGER.finer(ip.getHostAddress() + ":" + COMMPORT + " Not open deh or timeout");
                }

            } catch (IOException e) {
                Service.LOGGER.log(Level.SEVERE, "exception", e);
            }

        }


//        Service.LOGGER.warning("Stopping pinger " + factor);
    }

    public static void setIpBeginning(String ipBeginning) {
        PingerThread.ipBeginning = ipBeginning;
    }
}