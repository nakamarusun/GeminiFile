package com.geminifile.core.localhostconn;

import java.net.*;

//

public class LocalServerCommunicator{

    public static void startLocalServer() {
        Thread t = new Thread(new LocalServerThread());
        // t.setDaemon(true);
        t.start();
    }

}