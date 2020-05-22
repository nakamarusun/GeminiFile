package com.geminifile.core.service;

import com.geminifile.core.service.localhostconn.LocalServerCommunicator;
import com.geminifile.core.service.localnetworkconn.IpChangeChecker;
import com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.geminifile.core.CONSTANTS.*;

public class Service {

    private static InetAddress currentIp;
    private static Thread networkingThread;
    private static Node myNode;

    public static void start() {

        System.out.println("Service is starting...");
        // Starts local server message command processor
        LocalServerCommunicator.startLocalServer();

        // Assigns current thread as service thread.
        networkingThread = Thread.currentThread();

        while (true) {
            System.out.println("Networking service is starting...");

            // Checking thread.
            // Assigns the current ip address to the service variable
            try {
                currentIp = InetAddress.getLocalHost();
                String ip = currentIp.getHostAddress();
                // Sets a beginning for all of the pinger threads.
                PingerThread.setIpBeginning(ip.substring(0, ip.lastIndexOf('.') + 1));
            } catch (UnknownHostException e) {
                System.out.println("System cannot resolve a valid IP address !");
                e.printStackTrace();
                System.exit(-1);
            }

            // Generate unique SHA-256 based on identity
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                System.exit(5);
            }
            messageDigest.update((currentIp.getHostName() + System.getProperty("os.name")).getBytes());
            String uniqueId = new String(messageDigest.digest());

            // Assigns myNode
            myNode = new Node(currentIp,
                    COMMPORT,
                    uniqueId,
                    currentIp.getHostName(),
                    System.getProperty("os.name")
            );

            // If the current ip address is not localhost (connected to a network) then runs all of the networking service.
            // If not, then await for connection to be made.
            if (!currentIp.getHostAddress().startsWith("127")) {
                System.out.println(currentIp.getHostAddress());
                // Start pinger to ping all the ranges of the local ip address
                Thread pinger = new Thread(new ActivePeerGetter());
                pinger.setDaemon(true);
                pinger.start();

//                PeerCommunicatorManager.start();
            } else {
                System.out.println("Cannot start networking service, ip is " + currentIp.getHostAddress());
                System.out.println(myNode.toString());
            }


            // ScheduledExecutionService for checking whether self ip address is the same to warrant a restart.
            ScheduledExecutorService ipChecker = Executors.newSingleThreadScheduledExecutor();
            ipChecker.scheduleAtFixedRate(new IpChangeChecker(networkingThread), 5000, 5000, TimeUnit.MILLISECONDS);


//            Waits to detect any network ip changes, and restarts all of the networking services.
            try {
                networkingThread.join();
            } catch (InterruptedException e) {
                // Shutdowns the ip checker
                ipChecker.shutdownNow();
                // Interrupts the pinger and stops it
                ActivePeerGetter.stopService();
                // Stops all the PeerCommunicatorManager processes.
            }

        }
    }

    public static InetAddress getCurrentIp() {
        return currentIp;
    }

    public static void restartNetworkingService() {
        networkingThread.interrupt();
    }

    public static Node getMyNode() {
        return myNode;
    }

}