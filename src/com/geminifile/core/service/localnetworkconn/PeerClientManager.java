package com.geminifile.core.service.localnetworkconn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Vector;

import static com.geminifile.core.CONSTANTS.COMMPORT;

// Peer client manager accepts all connection from other devices running geminifile instances.

public class PeerClientManager implements Runnable {

    private static Vector<Thread> activeSocketPeers;
    private static ServerSocket ssock;

    private static boolean stopSsock = false;

    public PeerClientManager() {
        activeSocketPeers.clear();
        stopSsock = false;
    }

    @Override
    public void run() {
        try {
            ssock = new ServerSocket(COMMPORT, 50, InetAddress.getLocalHost());
            /* This works ONLY as connection acceptors.
            accepted connections operations are then put into a new separate thread.
            when another peer connects to this device, put the IP address to peerTable,
            to avoid double connections.
             */

            while (true) {
                // Creates new thread.
                Thread clientThread = new Thread(new PeerClientThread(ssock.accept()));
                clientThread.start();
                activeSocketPeers.add(clientThread);
            }

        } catch (IOException e) {
            if (stopSsock) {
                // Do stuff when stopped
            } else {
                System.out.println("Socket error");
                e.printStackTrace();
            }
        }
    }

    public static void stopService() {
        try {
            stopSsock = true;
            ssock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}