package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.MathUtil;

import java.io.File;
import java.io.Serializable;

// This is a class of an object that is sent through the network to the destined peer.
// Contains authentication token to verify its' integrity.
public class NetFile implements Serializable {

    private String token;
    private String filePathName; // Default path name is UNIX based, always convert to get it.
    private long fileSize;

    public NetFile(String token, String filePathName, long fileSize) {
        this.token = token;
        this.filePathName = filePathName;
        this.fileSize = fileSize;
    }

    public NetFile(String token, String filePathName, File file) {
        this.token = token;
        this.filePathName = filePathName;

        

    }

    public String getToken() {
        return token;
    }

    public String getFileName() {
        String[] fileDirectoryStructure = filePathName.split("/");
        return fileDirectoryStructure[fileDirectoryStructure.length - 1];
    }

    public long getFileSize() {
        return fileSize;
    }

    public static NetFile getEndFile() {
        return new NetFile("0", "0", 0);
    }

    public String getFilePathName() {
        return MathUtil.fileSeparatorToOS(filePathName);
    }

}
