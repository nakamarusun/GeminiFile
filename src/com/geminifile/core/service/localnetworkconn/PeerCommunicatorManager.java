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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerCommunicatorManager {

    // Stores the corresponding node with their sockets.
    private static final ConcurrentHashMap<Node, Socket> peerTable = new ConcurrentHashMap<>();

    private static Thread peerClient;
    private static Thread peerServer;

    public static void start() {

        // Run thread for PeerClientAcceptor
        peerClient = new Thread(new PeerClientManager(), "PeerClientManagerThread");
        peerClient.start();
        // Run thread for PeerServerSender
        peerServer = new Thread(new PeerServerManager(), "PeerServerManagerThread");
        peerServer.start();

    }

    public static void addPeerTable(Node node, Socket sock) {
        peerTable.put(node, sock);
    }

    public static void removePeerTable(Node node) {
        peerTable.remove(node);
    }

    public static boolean isInPeerTable(InetAddress inetAddr) {
        // Checks whether the specified ip is inside the peerTable
        for (Map.Entry<Node, Socket> e : peerTable.entrySet()) {
            if (e.getValue().getInetAddress() == inetAddr) {
                return true;
            }
        }
        return false;
    }

    public static void stopService() {
        // Disconnects all the peerTable
        for (Map.Entry<Node, Socket> e : peerTable.entrySet()) {
            try {
                e.getValue().close();
            } catch (IOException ioException) {
                System.out.println("Error closing socket" + e.toString());
                ioException.printStackTrace();
            }
        }
        // Clear peerTable when stopping service
        peerTable.clear();

        PeerClientManager.stopService();
        PeerServerManager.stopService();
    }

}