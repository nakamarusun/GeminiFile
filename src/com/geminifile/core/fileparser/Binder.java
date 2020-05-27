package com.geminifile.core.fileparser;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Binder {

    private String name; // Name of the binder
    private String id; // Id of the binder, enter this id on another machine to sync this binder
    private File directory; // Directory of the binder in the machine
    private final HashMap<String, Long> fileListing = new HashMap<>(); // A HashMap of all of the files recursively in the directory with their last modified time

    private long directoryLastModified;

    private final Lock fileListingLock = new ReentrantLock(true); // Lock to ensure fileListing access safety.

    private long lastTimePropertiesModified; // Last time any of the properties are modified.

    private final List<File> recurFiles = new ArrayList<>();

    // If the id is not specified in the constructor, then a length 7 random alphanumeric id will be generated
    public Binder(String name, String id, File directory) {
        this.name = name;
        this.id = id;
        this.directory = directory;
        lastTimePropertiesModified = new Date().getTime();
        directoryLastModified = directory.lastModified();
    }

    public Binder(String name, File directory) {
        this.name = name;
        this.id = generateRandomAlphaNum(7);
        this.directory = directory;
        lastTimePropertiesModified = new Date().getTime();
    }


    // Setters and getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        lastTimePropertiesModified = new Date().getTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        lastTimePropertiesModified = new Date().getTime();
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
        lastTimePropertiesModified = new Date().getTime();
    }


    // Custom methods
    public void update() {
        // Updates all of the file listing.

        fileListingLock.lock(); // Lock for safety
        fileListing.clear(); // Clears the list.
        try {
            // Loops within the new directory file listing and puts it into the hashmap.
            for (File e : listFilesRecursively(directory)) {
                String relPath = e.getPath().substring(directory.getPath().length());
                fileListing.put(relPath, e.lastModified());
            }
        } finally {
            fileListingLock.unlock();
        }
    }

    public HashMap<String, Long> getFileListing() {
        this.update();
        return fileListing;
    }

    public int randomRange(int min, int max) {
        Random rand = new Random();
        return (rand.nextInt(max - min + 1) + min);
    }

    public String generateRandomAlphaNum(int size) {
        StringBuilder str = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            switch (rand.nextInt() % 3) {
                case 0:
                    str.append( (char)randomRange(30, 39) );
                    break;
                case 1:
                    str.append( (char)randomRange(65, 90) );
                    break;
                case 2:
                    str.append( (char)randomRange(97, 122) );
                    break;
            }
        }
        return str.toString();
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

}