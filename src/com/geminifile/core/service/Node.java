package com.geminifile.core.service;

import java.net.InetAddress;

public class Node {

    private InetAddress ip;
    private int port;
    private String id;
    private String name;

    public Node(InetAddress ip, int port, String id, String name) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
