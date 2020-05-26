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
    private static final Set<InetAddress> activeIpAddresses = new HashSet<>();
    private static final Vector<InetAddress> tempIpAddresses = new Vector<>();
    private static final Lock updateListLock = new ReentrantLock(); // Lock to avoid from accessing list when updating

    private static final ArrayBlockingQueue<Set<InetAddress>> activeIpBQ = new ArrayBlockingQueue<>(1, true);

    private static boolean willStopService = false;

    private static Thread peerGetterThread;

    public ActivePeerGetter() {
        activeIpAddresses.clear();
        tempIpAddresses.clear();
        activeIpBQ.clear();
        willStopService = false;
    }


    @Override
    public void run() {

        System.out.println("Pinger service is starting...");
        // Sets reference to the thread running this process so can be interrupted.
        peerGetterThread = Thread.currentThread();

        // This section pings and collects active ip addresses
        long nextSync = 0;
        while (true) {
            tempIpAddresses.clear(); // Tries to clear tempIpAddresses

            ScheduledExecutorService pinger = Executors.newScheduledThreadPool(IPPINGERTHREADS, r -> new Thread(r, "Pinger"));// Creates new pinger scheduler
            List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
            long startTime = (new Date()).getTime(); // This stores the starting time of the pinger.


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
                    System.out.println("Peer getter thread interrupted");
                    // The waiting process is interrupted to stop the pinger service
                    if (willStopService) {
                        break;
                    }
                    // The waiting process is interrupted to restart the pinger service.
                    restartProcess = true;
                    break;
                } catch (ExecutionException e) {
                    System.out.println("Error in executing one of the pinger threads.");
                }
            }


            if (willStopService) {
                System.out.println("Peer getter thread stopping...");
                willStopService = false;
                // Stops service
                pinger.shutdownNow();
                break;
            }

            // If "refresh ips" is invoked then restart process.
            if (restartProcess) {
                System.out.println("Peer getter thread restarting...");
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
                // and make sure to remove any duplicates.
                activeIpAddresses.addAll(tempIpAddresses);
                // removes the current ip address from the other ip addresses.
                activeIpAddresses.remove(Service.getCurrentIp());
                // Offers the completed ip set to the queue.
                activeIpBQ.offer(activeIpAddresses);
            } finally {
                updateListLock.unlock();
            }

            // these run after all the pingerThreads and its' sub processes has completed
            pinger.shutdown();
            System.out.println("Process done after: " + ((new Date()).getTime() - startTime - nextSync));
            long startAfter = (PINGEVERYXSECOND * 1000) - ((new Date()).getTime() - startTime - nextSync);
            System.out.println("Process will restart after: " + startAfter);
            nextSync = startAfter > 0 ? startAfter : 0; // Renew the nextSync scheduler timer and clamp it so it never reaches below 0.
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
    public static Set<InetAddress> getUpdatedActiveIps() throws InterruptedException {
        return activeIpBQ.take();
    }

    static void addActiveTempIp(InetAddress ip) {
        tempIpAddresses.add(ip);
    }

    public static void stopService() {
        willStopService = true;
        peerGetterThread.interrupt();
    }

    public static void restartService() {
        peerGetterThread.interrupt();
    }

}