package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.ActivePeerGetter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static com.geminifile.core.CONSTANTS.COMMPORT;

// Peer client manager accepts all connection from other devices running geminifile instances.

public class PeerClientManager implements Runnable {

    private Vector<Socket> activeSocketPeers;

    @Override
    public void run() {
        try {
            ServerSocket ssock = new ServerSocket(COMMPORT, 50, InetAddress.getLocalHost());
            /* This works ONLY as connection acceptors.
            accepted connections operations are then put into a new separate thread.
            when another peer connects to this device, put the IP address to peerTable,
            to avoid double connections.
             */

            while (true) {
                activeSocketPeers.add(ssock.accept());
                // Creates new thread.
            }

        } catch (IOException e) {
            System.out.println("Socket error");
            e.printStackTrace();
        }

    }
}
