package com.geminifile.core.fileparser.netfilemanager;

// This is a class of an object that is sent through the network to the destined peer.
// Contains authentication token to verify its' integrity.
public class NetFile {

    private String token;
    private String fileName;
    private Byte[] file;

}
