package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import static com.geminifile.core.CONSTANTS.COMMPORT;

// Peer client manager accepts all connection from other devices running geminifile instances.

public class PeerClientManager implements Runnable {

    private static Vector<Thread> activeSocketPeers = new Vector<>();
    private static ServerSocket ssock;

    private static boolean stopSock;

    public PeerClientManager() {
        activeSocketPeers.clear();
        stopSock = false;
    }

    @Override
    public void run() {
        activeSocketPeers.clear();
        try {
            ssock = new ServerSocket(COMMPORT, 50, Service.getCurrentIp());
            /* This works ONLY as connection acceptors.
            accepted connections operations are then put into a new separate thread.
            when another peer connects to this device, put the IP address to peerTable,
            to avoid double connections.
             */

            while (true) {
                // Creates new thread.
                Socket socketRef = ssock.accept();
                PeerCommunicatorManager.lockPeerConnectionLock(); // Attempts to get lock
                try {
//                System.out.println("Connected with ip: " + socketRef.getInetAddress().getHostAddress());
                    // Check if the socket's ip is already in the peer table
                    if (PeerCommunicatorManager.isInPeerTable(socketRef.getInetAddress())) {
                        socketRef.close();
                        continue; // Closes the connection and continues.
                    }
                    Thread clientThread = new Thread(new PeerClientThread(socketRef), "PeerC" + socketRef.getInetAddress());
                    clientThread.start();
                    activeSocketPeers.add(clientThread);
                } finally {
                    PeerCommunicatorManager.unlockPeerConnectionLock();
                }
            }
        } catch (SocketException e) {
            if (stopSock) {
                // Stops the service
            } else {
                // Restart PeerClientManager.
            }
        } catch (IOException e) {
            System.out.println("[PEER] Socket error");
            e.printStackTrace();
        }
    }

    public static void stopService() {
        try {
            stopSock = true; // Signifies the interruption is because this method is invoked.
            ssock.close(); // Close ServerSocket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}