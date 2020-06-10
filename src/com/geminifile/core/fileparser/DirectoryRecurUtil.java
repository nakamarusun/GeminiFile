package com.geminifile.core.fileparser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectoryRecurUtil {

    private final List<File> recurFiles = new ArrayList<>(); // Private temp place to store the files.

    private final File directoryToRecur;

    public DirectoryRecurUtil(File directoryToRecur) {
        this.directoryToRecur = directoryToRecur;
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

    private List<File> listFilesRecursivelyWithDirectoryUtil(File path) {

        try {
            for (File e : Objects.requireNonNull(path.listFiles())) {
                // Loops through the list, if a directory is found, then recur.
                    recurFiles.add(e);
                if (e.isDirectory()) {
                    listFilesRecursivelyUtil(e);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("[BINDER] Folder is null");
            e.printStackTrace();
        }

        return recurFiles;
    }

    public List<File> listFilesRecursively() {

        // Clears the private variable first.
        recurFiles.clear();
        return listFilesRecursivelyUtil(directoryToRecur);
    }

    public List<File> listFilesRecursivelyWithDirectory() {

        // Clears the private variable first.
        recurFiles.clear();
        return listFilesRecursivelyWithDirectoryUtil(directoryToRecur);
    }

}
