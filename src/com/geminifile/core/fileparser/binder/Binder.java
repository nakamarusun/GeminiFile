package com.geminifile.core.fileparser.binder;

import com.geminifile.core.CONSTANTS;
import com.geminifile.core.fileparser.DirectoryRecurUtil;
import com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

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

    private Thread directoryWatcher = new Thread("BinderWatcher");
    private Thread binderUpdateWaiter = new Thread("UpdateWaiter");

    private boolean listingUpdated = false; // whether the FileListing has been updated or not. Sets to false if Watcher senses a change
    private boolean changeInBinderCheck = false; // If true, then restarts the waiting service.

    private WatchService mainWatcher;
    private Map<WatchKey, Path> keyDirectoryMap = new HashMap<>(); // The list of all of the directory watchers.

    private final List<FileListing> filesToSync = new ArrayList<>();

    private String fileToIgnore; // When watcher detects a file change, and the file's name is equal to this, then ignore operation.

    // If the id is not specified in the constructor, then a length 7 random alphanumeric id will be generated
    public Binder(String name, String id, File directory) {
        this.name = name;
        this.id = id;
        this.directory = directory;
        directoryLastModified = directory.lastModified();
        directoryWatcher.setName("BinderWatcher-" + id);
        binderUpdateWaiter.setName("UpdateWaiter-" + id);
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

    public void updateDirectoryLastModified() {
        directoryLastModified = directory.lastModified();
    }

    public String getFileToIgnore() {
        return fileToIgnore;
    }

    public void setFileToIgnore(String fileToIgnore) {
        this.fileToIgnore = fileToIgnore;
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
        System.out.println("[Binder] Starting directory watcher " + name);

        // Registers every directory inside of the binder.
        directoryWatcher = new Thread(() -> {
            try {

                // Clears all of the watcher fields.
                keyDirectoryMap.clear();
                changeInBinderCheck = false;

                // Initializes the main binder directory watcher
                mainWatcher = FileSystems.getDefault().newWatchService(); // New watcher service for filesystems
                registerSubWatcherService(directory);

                // Im really done with this project...
                // Initializes all of the sub-path directory watchers
                List<File> subFileListing = (new DirectoryRecurUtil(directory)).listFilesRecursivelyWithDirectory();
                for (File e : subFileListing) {
                    // Checks if the file is a directory
                    if (e.isDirectory()) {
//                        System.out.println("Registered: " + e.getAbsolutePath());
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
                        updateDirectoryLastModified();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            // Checks what events that happened.
                            WatchEvent.Kind<?> kind = event.kind(); // Kind of the modify operation.
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event; // Event object of the file that is modified.
                            Path path = keyDirectoryMap.get(key); // Directory of where the change is located.

                            String pathName = path.resolve(ev.context()).toString(); // Name of the file that is modified.
                            // Checks if the file is in any delta operations.
                            if (!pathName.equals(fileToIgnore)) {

                                // Creates a new watcher if a new directory has been created.
                                if (kind == ENTRY_CREATE && Files.isDirectory(path.resolve(ev.context()))) {
                                    try {
                                        registerSubWatcherService(new File(pathName));
                                    } catch (IOException e) {
                                        System.out.println("[Binder] Error adding new watcher to directory " + pathName);
                                        e.printStackTrace();
                                    }
                                }

                                // Puts the deletions, modifies and adds to the list
                                // If the current change is a file
                                if (!Files.isDirectory( path.resolve(ev.context()) )) {
                                    System.out.println("[Binder] Change in binder: " + kind.name() + " " + name + " @ " + pathName + ": " + new Date());
                                    String relPath = pathName.replace(directory.getAbsolutePath(), "");
                                    if (kind != ENTRY_DELETE) {
                                        // Adds this to the entry if it is not there yet
                                        boolean isThere = false;
                                        for (FileListing e : filesToSync) {
                                            if (e.getRelativePath().equals(relPath)) {
                                                isThere = true;
                                                break;
                                            }
                                        }
                                        if (!isThere) {
                                            try {
                                                filesToSync.add(new FileListing(relPath, new File(pathName)));
                                            } catch (IOException ignored) { }
                                        }
                                    } else {
                                        // Removes from the entry if it is there.
                                        filesToSync.removeIf(e -> e.getRelativePath().equals(relPath));
                                    }
                                    // Starts file change waiter thread
                                    changeInBinderCheck = true;
                                    if (!binderUpdateWaiter.isAlive()) {
                                        binderUpdateWaiter = new Thread(() -> {
                                            try {
                                                fileChangeWaiter();
                                            } catch (InterruptedException ignored) {
                                            }
                                        }, "UpdateWaiter-" + name);
                                        binderUpdateWaiter.start();
                                    }
                                }


                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                    } catch (InterruptedException ex) {
                        // Quit the thread
                        System.out.println("[Binder] Closing watcher " + id);
                        mainWatcher.close();
                        return;
                    } catch (NoSuchAlgorithmException e) {
                        System.out.println("[Binder] Wrong algorithm in the file hashing.");
                        e.printStackTrace();
                    }

                }

            } catch (IOException e) {
                System.out.println("[Binder] Error creating watcher");
                e.printStackTrace();
            }
        }, "BinderWatcher-" + id);

        directoryWatcher.start();

    }

    public void registerSubWatcherService(File directory) throws IOException {
        // Registers the watcher service of the current's folder in the list.
        Path subDir = Paths.get(directory.getAbsolutePath());
        // Checks if there is already the same path registered.
        for (Path e : keyDirectoryMap.values()) {
            if (e.toString().equals(subDir.toString())) {
                return;
            }
        }
        WatchKey key = subDir.register(mainWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        System.out.println("Registered " + subDir.toString() + " to directory watcher");

        keyDirectoryMap.put(key, subDir); // Puts the keyDirectoryMap
    }

    private void fileChangeWaiter() throws InterruptedException {
        // Binder folder waiter for another change in the binder, so that when one change registers, does not immediately sync.
        // Object lock waiting
        Object obj = new Object();
        synchronized (obj) {
            do {
                changeInBinderCheck = false;
                obj.wait(CONSTANTS.WAITUPDATEFOR * 1000);
            } while (changeInBinderCheck);
        }
        System.out.println("[Binder] " + filesToSync.size() + " Change detected in binder '" + name + "'. Will query to other peers.");
        queryFileWithOtherPeers();
    }

    private void queryFileWithOtherPeers() {
        if (filesToSync.size() == 0) return;
        // Do the send operations here.
        BinderFileDelta delta = new BinderFileDelta(id);
        System.out.println("Will send files:");
        for (FileListing e : filesToSync) {
            delta.addOtherPeerNeed(e.getRelativePath());
            System.out.println(e.getRelativePath());
        }
        BinderManager.addBinderDeltaOperation(delta);
        PeerCommunicatorManager.sendToAllPeers(new MsgWrapper("WantToSync-" + id + ":" + delta.getToken() + ":" + filesToSync.size(), MsgType.ASK));
        filesToSync.clear();
    }

    public void stopWatcher() {
        if (directoryWatcher.isAlive()) {
            directoryWatcher.interrupt();
        }
    }
}