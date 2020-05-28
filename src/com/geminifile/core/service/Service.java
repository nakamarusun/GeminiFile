package com.geminifile.core.service;

import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.localhostconn.LocalServerCommunicator;
import com.geminifile.core.service.localnetworkconn.IpChangeChecker;
import com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.geminifile.core.CONSTANTS.COMMPORT;

public class Service {

    private static InetAddress currentIp; // Ip getting must be the first non-loopback address.
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
                // Get the first non-loopback address
                currentIp = Service.getNonLoopbackIp4Address();
                String ip = currentIp.getHostAddress();
                // Sets a beginning for all of the pinger threads.
                PingerThread.setIpBeginning(ip.substring(0, ip.lastIndexOf('.') + 1));
                System.out.println("ip is: " + currentIp.getHostAddress());
            } catch (SocketException e) {
                System.out.println("System cannot resolve a valid IP address !");
                e.printStackTrace();
                System.exit(-1);
            }

            // Generate unique SHA-256 based on identity
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] hash;
                // The input for the hash is basically the machine MAC address. If doesn't exist then, (machine name + os name)
                byte[] macAddr = NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress();
                if (macAddr == null) {
                    hash = messageDigest.digest((currentIp.getHostName() + System.getProperty("os.name")).getBytes(StandardCharsets.UTF_8)); // Update the bytes with the string converted by the UTF-8 format
                } else {
                    hash = messageDigest.digest(macAddr); // Update the bytes with the string converted by the UTF-8 format
                }
                StringBuilder hexString = new StringBuilder();

                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String uniqueId = hexString.toString();
                // Assigns myNode
                myNode = new Node(currentIp,
                        COMMPORT,
                        uniqueId,
                        currentIp.getHostName(),
                        System.getProperty("os.name")
                );
            } catch (NoSuchAlgorithmException | SocketException e) {
                e.printStackTrace();
                System.exit(5);
            }


            // If the current ip address is not localhost (connected to a network) then runs all of the networking service.
            // If not, then await for connection to be made.
            if (!currentIp.getHostAddress().startsWith("127")) {
                // Start pinger to ping all the ranges of the local ip address
                Thread pinger = new Thread(new PingerManager(), "PingerManager");
                pinger.setDaemon(true);
                pinger.start();

                // Starts PeerCommunicatorManager to connect and query with other peers, and to start the communication loop.
                PeerCommunicatorManager.start();

                // Starts folder checker service
                BinderManager.start();

            } else {
                System.out.println("Cannot start networking service, ip is " + currentIp.getHostAddress());
                System.out.println(myNode.toString());
            }

            // ScheduledExecutionService for checking whether self ip address is the same to warrant a restart.
            // Use lambda ThreadFactory to name the thread.
            ScheduledExecutorService ipChecker = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "NetworkChangeDetector"));
            ipChecker.scheduleAtFixedRate(new IpChangeChecker(networkingThread), 5000, 5000, TimeUnit.MILLISECONDS);


//            Waits to detect any network ip changes, and restarts all of the networking services.
            try {
                networkingThread.join();
            } catch (InterruptedException e) {
                // Shutdowns the ip checker
                ipChecker.shutdownNow();
                // Interrupts the pinger and stops it
                PingerManager.stopService();
                // Stops all the PeerCommunicatorManager processes.
                PeerCommunicatorManager.stopService();
            }
        }
    }

    public static InetAddress getCurrentIp() {
        return currentIp;
    }

    public static void restartNetworkingService() {
        // Interrupts the main service thread, signifying to restart service.
        networkingThread.interrupt();
    }

    public static Node getMyNode() {
        return myNode;
    }

    public static InetAddress getNonLoopbackIp4Address() throws SocketException {
        // Gets the first non loopback ipv4 address, and returns it. If there is none, then return the loopbackAddress.
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress address = en2.nextElement();
                if (!address.isLoopbackAddress()) {
                    if (address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
        }
        return InetAddress.getLoopbackAddress();
    }

}