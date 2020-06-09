package com.geminifile.core.fileparser.binder;

import org.json.JSONObject;

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

    private ArrayList<FileListing> fileListing = new ArrayList<>(); // A HashMap of all of the files recursively in the directory with their last modified time
    private final Lock fileListingLock = new ReentrantLock(true); // Lock to ensure fileListing access safety.
    private final List<File> recurFiles = new ArrayList<>();

    private Thread directoryWatcher = new Thread();

    private boolean updated = false; // whether the FileListing has been updated or not. Sets to false if Watcher senses a change

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
            for (File e : listFilesRecursively(directory)) {
                String relPath = e.getPath().substring(directory.getPath().length());
                FileListing currentFile = new FileListing(relPath, e);
                fileListing.add(currentFile);
            }
            updated = true;
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
        if (!updated) {
            this.update();
        }
        return fileListing;
    }

    private List<File> listFilesRecursivelyUtil(File path) {

        try {
            for (File e : Objects.requireNonNull(path.listFiles())) {
                // Loops through the list, if a directory is found, then recur.
                if (e.isFile()) {
                    recurFiles.add(e);
                } else if (e.isDirectory()) {
                    listFilesRecursivelyUtil(e);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("[BINDER] Folder is null");
            e.printStackTrace();
        }

        return recurFiles;
    }

    public List<File> listFilesRecursively(File path) {

        // Clears the private variable first.
        recurFiles.clear();
        return listFilesRecursivelyUtil(path);
    }

    public String getFileListingJSON() {
        String result = "";

        return result;
    }

    public void startWatcher() {

        directoryWatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WatchService watcher = FileSystems.getDefault().newWatchService(); // New watcher service for filesystems
                    Path dir = Paths.get(directory.getAbsolutePath()); // The path of the directory to check
                    dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY); // Registers the watcher to the directory, with the following args to watch for

                    // Always checking for difference
                    while (true) {
                        WatchKey key; // key object
                        try {
                            key = watcher.take();
                        } catch (InterruptedException ex) {
                            // Quit the thread
                            return;
                        }



                        // At this point, the service has detected a change in the directory it is watching.
                        System.out.println("[FILE] Change in binder: " + name + " @ " + directory.getAbsolutePath() + ": " + new Date() );
                        updated = false; // Sets updated status to false.

//                        for (WatchEvent<?> event : key.pollEvents()) {
//                            // Checks what events that happened.
//                            // TODO: Wait for several seconds before syncing with the other machine
//                            WatchEvent.Kind<?> kind = event.kind();
//
//                            @SuppressWarnings("unchecked")
//                            WatchEvent<Path> ev = (WatchEvent<Path>) event; //
//
//                            }

                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }

                        key.pollEvents();   // Removes all queue event.
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                    }

                } catch (IOException e) {
                    System.out.println("Error creating watcher");
                    e.printStackTrace();
                }
            }
        }, "BinderWatcher-" + id);

        directoryWatcher.start();

    }

    public void stopWatcher() {
        if (directoryWatcher.isAlive()) {
            directoryWatcher.interrupt();
        }
    }
}