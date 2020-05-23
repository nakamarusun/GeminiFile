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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager {

    private static final Set<Node> peerTable = new HashSet<>(); // When current device has connected to another device, insert into set.
    private static final Lock peerTableLock = new ReentrantLock(); // Concurrency safety.

    public static void start() {

        // Run thread for PeerClientAcceptor
        Thread peerClient = new Thread(new PeerClientManager());
        peerClient.start();
        // Run thread for PeerServerSender

    }

    public static void addPeerTable(Node node) {
        peerTableLock.lock();
        try {
            peerTable.add(node);
        } finally {
            peerTableLock.unlock();
        }
    }

    public static void removePeerTable(Node node) {
        peerTableLock.lock();
        try {
            peerTable.remove(node);
        } finally {
            peerTableLock.unlock();
        }
    }

    public static void stopService() {
        PeerClientManager.stopService();
        // Clear peerTable when starting service
        peerTable.clear();
    }

}