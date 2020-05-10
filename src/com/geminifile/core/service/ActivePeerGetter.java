package com.geminifile.core.service;

import java.net.InetAddress;
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
    private static Lock updateListLock;

    public ActivePeerGetter() {
        tempIpAddresses = new Vector<>();
    }

    @Override
    public void run() {

        updateListLock = new ReentrantLock();
        // This section pings and collects active ip addresses
        long nextSync = 0;
        while (true) {

            ScheduledExecutorService pinger = Executors.newScheduledThreadPool(IPPINGERTHREADS);// Creates new pinger scheduler
            List<ScheduledFuture<?>> threads = new ArrayList<>();
            long startTime = (new Date()).getTime(); // This stores the starting time of the pinger.

            // Starts executing the pings
            for (int i = 0; i < IPPINGERTHREADS; i++) {
                threads.add(pinger.schedule(new PingerThread(i), nextSync, TimeUnit.MILLISECONDS));
            }


            // This method checks whether all of the pingerThreads has completed.
            for (ScheduledFuture<?> trd : threads) {
                try {
                    trd.get();
                } catch (InterruptedException | ExecutionException ignored) { }
            }


            // More process after pings is done
            // This section covers the renewing of the address database
            updateListLock.lock(); // Blocks the process, so cannot access
            try {
                activeIpAddresses.clear(); // Tries to clear the activeIpAddresses
            } catch (Exception ignored) { }
            for (int i = 0; i < tempIpAddresses.size(); i++) {
                activeIpAddresses.add(tempIpAddresses.get(0));
            }
            try {
                tempIpAddresses.clear(); // Tries to clear tempIpAddresses
            } catch (Exception ignored) { }
            updateListLock.unlock();


            // these run after all the pingerThreads and its' sub processes has completed
            pinger.shutdown();
            System.out.println("Process done after: " + ((new Date()).getTime() - startTime - nextSync));
            long startAfter = (PINGEVERYXSECOND * 1000) - ((new Date()).getTime() - startTime - nextSync);
            System.out.println("Process will restart after: " + startAfter);
            nextSync = startAfter > 0 ? startAfter : 0; // Renew the nextSync scheduler timer
        }

    }

    public static Set<InetAddress> getUpdatedActiveIp() {
        // Check if blocked
        updateListLock.lock();
        updateListLock.unlock();
        return activeIpAddresses;
    }

    static void addActiveTempIp(InetAddress ip) {
        tempIpAddresses.add(ip);
    }
}