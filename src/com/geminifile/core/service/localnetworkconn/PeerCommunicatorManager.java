package com.geminifile.core.service.localnetworkconn;

/*
This class manages the peer to peer communication services.
Connects to all of the peers available from ActivePeerGetter.activeIpAddresses
====================
There will be two main kinds of Peer managers, PeerClientAcceptor manages the new
incoming connections from another peer,
meanwhile PeerServerSender manages new connections to another device.
 */

import com.geminifile.core.service.Node;

import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager {

    private static final Vector<Node> peerTable = new Vector<>(); // When current device has connected to another device, insert into set.

    private static Thread peerClient;
    private static Thread peerServer;

    public static void start() {

        // Run thread for PeerClientAcceptor
        peerClient = new Thread(new PeerClientManager());
        peerClient.start();
        // Run thread for PeerServerSender
        peerServer = new Thread(new PeerServerManager());
        peerServer.start();

    }

    public static void addPeerTable(Node node) {
        peerTable.add(node);
    }

    public static void removePeerTable(Node node) {
        peerTable.remove(node);
    }

    public static void stopService() {
        PeerClientManager.stopService();
        // Clear peerTable when starting service
        peerTable.clear();
    }

}