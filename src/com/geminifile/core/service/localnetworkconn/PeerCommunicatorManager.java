package com.geminifile.core.service.localnetworkconn;

/*
This class manages the peer to peer communication services.
Connects to all of the peers available from ActivePeerGetter.activeIpAddresses
 */

import com.geminifile.core.service.Node;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager implements Runnable {

    private static Set<Node> peerTable;
    private static Lock peerTableLock;

    static {
        peerTableLock = new ReentrantLock();
    }

    @Override
    public void run() {



    }

    public static void addPeerTable(Node node) {
        peerTableLock.lock();
        peerTable.add(node);
        peerTableLock.unlock();
    }

    public static void removePeerTable(Node node) {
        peerTableLock.lock();
        peerTable.remove(node);
        peerTableLock.unlock();
    }

}