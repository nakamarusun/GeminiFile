package com.geminifile.core.service.localnetworkconn;

import java.io.IOException;
import java.net.ServerSocket;

import static com.geminifile.core.CONSTANTS.COMMPORT;

public class PeerClientAcceptor implements Runnable {

    @Override
    public void run() {
        try {
            ServerSocket ssock = new ServerSocket(COMMPORT);
        } catch (IOException e) {
            System.out.println("Socket error");
            e.printStackTrace();
        }
    }
}
