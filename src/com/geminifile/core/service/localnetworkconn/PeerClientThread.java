package com.geminifile.core.service.localnetworkconn;

import java.net.Socket;

public class PeerClientThread implements Runnable {

    Socket mySocket;

    public PeerClientThread(Socket mySocket) {
        this.mySocket = mySocket;
    }

    @Override
    public void run() {
        // Accept query from the peer.
    }

}
