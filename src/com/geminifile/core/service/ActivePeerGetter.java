package com.geminifile.core.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.geminifile.core.CONSTANTS.*;

/* This service runs every X seconds, to get all the concurrently running geminifile services
in the network.
 */

public class ActivePeerGetter implements Runnable {
    // Vector is used here because of its' safety feature for multithreading workloads.
    private static Set<InetAddress> activeIpAddresses;
    private static Vector<InetAddress> tempIpAddresses;
    private static final Lock updateListLock; // Lock to avoid from accessing list when updating

    private static boolean isIpUpdated;
    private static final Lock isIpUpdatedLock;

    private static InetAddress currentAddress;


    static {
        updateListLock = new ReentrantLock();
        isIpUpdatedLock = new ReentrantLock();
        tempIpAddresses = new Vector<>();
        isIpUpdated = false;
    }

    @Override
    public void run() {

        // This section pings and collects active ip addresses
        long nextSync = 0;
        while (true) {

            // Get current local IP Address. If got 127.0.0.1, then for now quite the program.
            // Checks the status of network. Is the device connected to any network ?
            InetAddress id;
            try {
                id = InetAddress.getLocalHost();
                String ip = id.getHostAddress();
                if (ip.equals("127.0.0.1")) {
                    // If current device is not connected to any network, then run a thread to detect if the device is then connected to any network
                    System.out.println("System is not connected to any network !");
                    // If current thread is interrupted by the ipChecker thread, then restart the service.
                    try {
                        Thread.currentThread().join();
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                // Sets the current ip address
                currentAddress = id;
                // SETS THE IP BEGINNING FOR PINGER THREAD
                PingerThread.setIpBeginning(ip.substring(0, ip.lastIndexOf('.') + 1));
            } catch(UnknownHostException e) {
                System.out.println("System cannot resolve a valid address !");
                System.exit(-1);
            }

            ScheduledExecutorService pinger = Executors.newScheduledThreadPool(IPPINGERTHREADS);// Creates new pinger scheduler
            List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
            long startTime = (new Date()).getTime(); // This stores the starting time of the pinger.

            // If haven't been updated yet, then lock the process.
            try {
                if (!isIpUpdated) {
                    isIpUpdatedLock.lock();
                }

                // Starts executing the pings
                // How do i interrupt this process ?
                for (int i = 0; i < IPPINGERTHREADS; i++) {
                    scheduledFutures.add(pinger.schedule(new PingerThread(i), nextSync, TimeUnit.MILLISECONDS));
                }


                // This method checks whether all of the pingerThreads has completed.
                // This boolean functions as a override for the "refresh now" function.
                boolean restartProcess = false;
                for (ScheduledFuture<?> trd : scheduledFutures) {
                    try {
                        trd.get();
                    } catch (InterruptedException interrupted) {
                        // The waiting process is interrupted to restart the pinger service.
                        restartProcess = true;
                        break;
                    } catch (ExecutionException e) {
                        System.out.println("Error in executing one of the pinger threads.");
                    }
                }

                // If "refresh ips" is invoked then restart process.
                if (restartProcess) {
                    pinger.shutdownNow(); // shutdown all of the thread processes.
                    nextSync = 0;
                    continue;
                }


                // More process after pings is done
                // This section covers the renewing of the address database
                updateListLock.lock(); // Blocks the process, so cannot access
                try {
                    activeIpAddresses.clear(); // Tries to clear the activeIpAddresses
                    // adds address from temporary to active
                    activeIpAddresses.addAll(tempIpAddresses);
                    // removes the current ip address from the other ip addresses.
                    activeIpAddresses.remove(currentAddress);
                    tempIpAddresses.clear(); // Tries to clear tempIpAddresses
                } finally {
                    updateListLock.unlock();
                }
            } finally {
                // Updating process is done.
                if (!isIpUpdated) {
                    isIpUpdatedLock.unlock();
                }
            }

            // Signs that ip table has been updated
            isIpUpdated = true;

            // these run after all the pingerThreads and its' sub processes has completed
            pinger.shutdown();
            System.out.println("Process done after: " + ((new Date()).getTime() - startTime - nextSync));
            long startAfter = (PINGEVERYXSECOND * 1000) - ((new Date()).getTime() - startTime - nextSync);
            System.out.println("Process will restart after: " + startAfter);
            nextSync = startAfter > 0 ? startAfter : 0; // Renew the nextSync scheduler timer
        }

    }

    // Get all active ips
    public static Set<InetAddress> getActiveIps() {
        // Check if blocked
        updateListLock.lock();
        updateListLock.unlock();
        return activeIpAddresses;
    }

    // Get all updated active ips. When method is invoked again, but ip table has not been updated, then wait.
    public static Set<InetAddress> getUpdatedActiveIps() {
        isIpUpdatedLock.lock();
        isIpUpdatedLock.unlock();
        isIpUpdated = false;
        return activeIpAddresses;
    }

    static void addActiveTempIp(InetAddress ip) {
        tempIpAddresses.add(ip);
    }

    public static InetAddress getCurrentAddress() {
        return currentAddress;
    }

}