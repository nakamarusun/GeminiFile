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

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager {

    private static Set<Node> peerTable; // When current device has connected to another device, insert into set.
    private static final Lock peerTableLock; // Concurrency safety.

    static {
        peerTableLock = new ReentrantLock();
    }

    public void start() {
        // When a network ip is reset, or connection is reset, restart all of the thread.
        while (true) {
            // Run thread for PeerClientAcceptor
            // Run thread for PeerServerSender

            /* ScheduledExecutionService for checking whether self ip address is the same
            to warrant a restart.
             */
            ScheduledExecutorService ipChecker = Executors.newSingleThreadScheduledExecutor();

            // If interrupted, stop thread and restart service.
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignored) { }
        }
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

}