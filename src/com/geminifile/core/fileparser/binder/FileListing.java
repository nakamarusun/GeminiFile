package com.geminifile.core.fileparser.binder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileListing {

    private final String relativePath;
    private final long lastModified;
    private final String checkSumMD5;

    public FileListing(String relativePath, File file) throws NoSuchAlgorithmException, IOException {
        this.relativePath = relativePath;
        lastModified = file.lastModified();
        // Gets the MD5 sum
        MessageDigest md5 = MessageDigest.getInstance("MD5"); // Gets the algorithm
        FileInputStream fileStream = new FileInputStream(file); // Opens input stream
        byte[] buffer = new byte[1024]; // Creates buffer

        int fileUntil;

        // Reads file and inserts it into the md5 algorithm
        do {
            fileUntil = fileStream.read(buffer);
            if (fileUntil > 0) {
                md5.update(buffer, 0, fileUntil);
            }
        } while (fileUntil > 0);

        // Converts the hash byte array to string
        byte[] hashByte = md5.digest();
        StringBuilder finalHash = new StringBuilder();

        for (byte b : hashByte) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) finalHash.append('0');
            finalHash.append(hex);
        }

        fileStream.close(); // Don't forget to close the stream

        checkSumMD5 = finalHash.toString();
    }

    public FileListing(String relativePath, long lastModified, String checkSumMD5) {
        this.relativePath = relativePath;
        this.lastModified = lastModified;
        this.checkSumMD5 = checkSumMD5;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getCheckSumMD5() {
        return checkSumMD5;
    }

    @Override
    public String toString() {
        return "FileListing{" +
                "relativePath='" + relativePath + '\'' +
                ", lastModified=" + lastModified +
                ", checkSumMD5='" + checkSumMD5 + '\'' +
                '}';
    }
}