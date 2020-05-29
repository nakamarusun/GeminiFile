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
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager {

    // Stores the corresponding node with their sockets.
    private static final Vector<PeerCommunicationLoop> peerTable = new Vector<>();

    private static Thread peerClient;
    private static Thread peerServer;

    private static Lock peerConnectionLock = new ReentrantLock(); // Lock to prevent the device from accepting connections while attempting to connect to other machines.

    public static void start() {

        System.out.println("[PEER] Starting Peer Communicator Manager...");
        // Run thread for PeerClientAcceptor
        peerClient = new Thread(new PeerClientManager(), "PeerClientManager");
        peerClient.start();
        // Run thread for PeerServerSender
        peerServer = new Thread(new PeerServerManager(), "PeerServerManager");
        peerServer.start();

    }

    public static void addPeerTable(PeerCommunicationLoop comms) {
        peerTable.add(comms);
    }

    public static void removePeerTable(Node node) {
        peerTable.removeIf(e -> e.getNode() == node);
    }

    public static boolean isInPeerTable(InetAddress inetAddr) {
        // Checks whether the specified ip is inside the peerTable
        for (PeerCommunicationLoop e : peerTable) {
            if (e.getSock().getInetAddress().equals(inetAddr)) {
                return true;
            }
        }
        return false;
    }

    public static void stopService() {
        // Disconnects all the peerTable
        for (PeerCommunicationLoop e : peerTable) {
            try {
                e.getSock().close();
            } catch (IOException ioException) {
                System.out.println("[PEER] Error closing socket" + e.toString());
                ioException.printStackTrace();
            }
        }
        // Clear peerTable when stopping service
        peerTable.clear();

        PeerClientManager.stopService();
        PeerServerManager.stopService();
    }

    public static void sendToAllPeers(MsgWrapper msg) {
        // Sends the specified message to all of the  peer table.
        for (PeerCommunicationLoop e : peerTable) {
            e.sendMsg(msg);
        }
    }

    public static void lockPeerConnectionLock() {
        peerConnectionLock.lock();
    }

    public static void unlockPeerConnectionLock() {
        peerConnectionLock.unlock();
    }

}