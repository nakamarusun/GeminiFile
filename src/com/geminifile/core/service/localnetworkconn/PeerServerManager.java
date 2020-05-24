package com.geminifile.core.service.localnetworkconn;

// PeerServerManager gets updated ips from ActivePeerGetter and tries to connect to them here.
// Requests will be send to PeerClientManager in the other's server geminifile instance.

import com.geminifile.core.service.ActivePeerGetter;

import java.net.InetAddress;
import java.util.Set;
import java.util.Vector;

public class PeerServerManager implements Runnable {

    private static Vector<Thread> activeSocketPeers = new Vector<>();
    private static Thread currentThread;

    @Override
    public void run() {
        // Set current thread as reference
        currentThread = Thread.currentThread();

        while (true) {
            try {
                // Get updated ip list, will block to wait for updated peer from ActivePeerGetter
                Set<InetAddress> peerList = ActivePeerGetter.getUpdatedActiveIps();

                for (InetAddress e : peerList) {
                    // Creates a new thread to process the connection.
                    Thread serverThread = new Thread(new PeerServerThread(e), "PeerThreadS" + e.getHostAddress());
                    serverThread.start();
                    activeSocketPeers.add(serverThread);
                }

            } catch (InterruptedException e) {
                // The service is stopped
                return;
            }
        }

    }

    public static void stopService() {
        currentThread.interrupt();
    }

}
