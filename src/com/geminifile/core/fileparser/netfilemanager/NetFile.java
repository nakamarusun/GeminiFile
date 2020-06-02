package com.geminifile.core.fileparser.netfilemanager;

// This is a class of an object that is sent through the network to the destined peer.
// Contains authentication token to verify its' integrity.
public class NetFile {

    private String token;
    private String fileName;
    private Byte[] file;

    public NetFile(String token, String fileName, Byte[] file) {
        this.token = token;
        this.fileName = fileName;
        this.file = file;
    }

    public String getToken() {
        return token;
    }

    public String getFileName() {
        return fileName;
    }

    public Byte[] getFile() {
        return file;
    }

    public static NetFile getEndFile() {
        return new NetFile("0", "0", new Byte[1]);
    }

}
