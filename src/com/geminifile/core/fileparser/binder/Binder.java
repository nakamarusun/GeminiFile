package com.geminifile.core.fileparser.binder;

import com.geminifile.core.CONSTANTS;
import com.geminifile.core.fileparser.DirectoryRecurUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.geminifile.core.MathUtil.generateRandomAlphaNum;
import static java.nio.file.StandardWatchEventKinds.*;

public class Binder {

    private String name; // Name of the binder
    private String id; // Id of the binder, enter this id on another machine to sync this binder // TODO: SECURITY ISSUE: DANGEROUS TO SEND ID TO EVERY PEER, HASH IT.
    private File directory; // Directory of the binder in the machine
    private long directoryLastModified;

    private final ArrayList<FileListing> fileListing = new ArrayList<>(); // A HashMap of all of the files recursively in the directory with their last modified time
    private final Lock fileListingLock = new ReentrantLock(true); // Lock to ensure fileListing access safety.

    private Thread directoryWatcher = new Thread();
    private Thread binderUpdateWaiter = new Thread();

    private boolean listingUpdated = false; // whether the FileListing has been updated or not. Sets to false if Watcher senses a change
    private boolean sendQuery = false; // If this is true, when the thread is interrupted, sends queries about the files.
    private boolean changeInBinderCheck = false; // If true, then restarts the waiting service.

    private WatchService mainWatcher;
    private Map<WatchKey, Path> keyDirectoryMap = new HashMap<>(); // The list of all of the directory watchers.

    // If the id is not specified in the constructor, then a length 7 random alphanumeric id will be generated
    public Binder(String name, String id, File directory) {
        this.name = name;
        this.id = id;
        this.directory = directory;
        directoryLastModified = directory.lastModified();
    }

    public Binder(String name, File directory) {
        this.name = name;
        this.id = generateRandomAlphaNum(7);
        this.directory = directory;
        directoryLastModified = directory.lastModified();
    }

    //region Setters and getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public long getDirectoryLastModified() {
        return directoryLastModified;
    }

    public void setDirectoryLastModified(long directoryLastModified) {
        this.directoryLastModified = directoryLastModified;
    }
    //endregion

    // Custom methods
    private void update() {
        // Updates all of the file listing.

        fileListingLock.lock(); // Lock for safety
        fileListing.clear(); // Clears list
        try {
            // Loops within the new directory file listing and puts it into the hashmap.
            DirectoryRecurUtil recurUtil = new DirectoryRecurUtil(directory);
            for (File e : recurUtil.listFilesRecursively()) {
                String relPath = e.getPath().substring(directory.getPath().length());
                FileListing currentFile = new FileListing(relPath, e);
                fileListing.add(currentFile);
            }
            listingUpdated = true;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Binder] Error getting hashing algorithm");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[Binder] Error updating binder listing");
            e.printStackTrace();
        } finally {
            fileListingLock.unlock();
        }
    }

    public ArrayList<FileListing> getFileListing() {
        if (!listingUpdated) {
            this.update();
        }
        return fileListing;
    }

    public String getFileListingJSON() {
        String result = "";

        return result;
    }

    public void startWatcher() {

        // If thread is already running, don't run.
        if (directoryWatcher.isAlive()) {
            return;
        }

        // Registers every directory inside of the binder.
        directoryWatcher = new Thread(() -> {
            try {

                // Clears all of the watcher fields.
                keyDirectoryMap.clear();
                sendQuery = false;
                changeInBinderCheck = false;

                // Initializes the main binder directory watcher
                mainWatcher = FileSystems.getDefault().newWatchService(); // New watcher service for filesystems
                registerSubWatcherService(directory);

                // Initializes all of the sub-path directory watchers
                List<File> subFileListing = (new DirectoryRecurUtil(directory)).listFilesRecursivelyWithDirectory();
                for (File e : subFileListing) {
                    // Checks if the file is a directory
                    if (e.isDirectory()) {
                        registerSubWatcherService(e); // Registers the the new folder.
                    }
                }

                // Always checking for difference
                while (true) {
                    WatchKey key; // key object
                    try {
                        key = mainWatcher.take();

                        // At this point, the service has detected a change in the directory it is watching.
                        listingUpdated = false; // Sets updated status to false.

                        for (WatchEvent<?> event : key.pollEvents()) {
                            // Checks what events that happened.
                            WatchEvent.Kind<?> kind = event.kind(); // Kind of the modify operation.

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event; // Name of the file that is modified.

                            Path path = keyDirectoryMap.get(key); // Directory of where the change is located.

                            System.out.println("[Binder] Change in binder: " + kind.name() + " " + name + " @ " + path.resolve(ev.context()) + ": " + new Date());

                            // Puts the deletions, modifies and adds to the list


                            // Starts file change waiter thread
                            changeInBinderCheck = true;
                            if (!binderUpdateWaiter.isAlive()) {
                                binderUpdateWaiter = new Thread(() -> { try { fileChangeWaiter(); } catch (InterruptedException ignored) {} }, "Waiter-" + name);
                                binderUpdateWaiter.start();
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                    } catch (InterruptedException ex) {
                        if (sendQuery) {
                            queryFileWithOtherPeers(); // If thread is interrupted but the interrupt is about querying then send files.
                        } else {
                            // Quit the thread
                            mainWatcher.close();
                            return;
                        }
                    }

                }

            } catch (IOException e) {
                System.out.println("[Binder] Error creating watcher");
                e.printStackTrace();
            }
        }, "BinderWatcher-" + id);

        directoryWatcher.start();

    }

    private void registerSubWatcherService(File directory) throws IOException {
        // Registers the watcher service of the current's folder in the list.
        Path subDir = Paths.get(directory.getAbsolutePath());
        WatchKey key = subDir.register(mainWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        keyDirectoryMap.put(key, subDir); // Puts the keyDirectoryMap
    }

    private void fileChangeWaiter() throws InterruptedException {
        // Binder folder waiter for another change in the binder, so that when one change registers, doesnt immediately sync.
        // Object lock waiting
        Object obj = new Object();
        synchronized (obj) {
            do {
                changeInBinderCheck = false;
                obj.wait(CONSTANTS.WAITUPDATEFOR * 1000);
            } while (changeInBinderCheck);
        }
        System.out.println("[Binder] X Change detected in binder '" + name + "'. Will query to other peers.");
//        sendQuery = true;
//        directoryWatcher.interrupt();
    }

    private void queryFileWithOtherPeers() {

    }

    public void stopWatcher() {
        if (directoryWatcher.isAlive()) {
            directoryWatcher.interrupt();
        }
    }
}