package com.geminifile.core.localhostconn;

import com.geminifile.core.socketmsg.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

/*
This class is used to set up a localhost server.
When the geminifile service starts, it will run in the background with little control of how it runs.
This module is used to query information and send commands towards that service.
By setting up a local host server, it can receive and send messages and commands.
 */

public class LocalServerThread implements Runnable {

    private ServerSocket ssock;
    private Socket sock;

    @Override
    public void run() {
        try {
            // TODO: MAKE SERVER REQUIRE AUTHENTICATION BEFORE CONNECTING

            System.out.println("Opening port in " + LOCALPORT);
            ssock = new ServerSocket(LOCALPORT);

            sock = ssock.accept(); // Accepts connection
            System.out.println("Connected !");

            ObjectInputStream os = new ObjectInputStream(sock.getInputStream());

            while(true) {
                try {
                    MsgWrapper msg = (MsgWrapper) os.readObject();
                    LocalServerCommunicator.putMessage(msg);
                    // TODO: PAUSE THREAD AND SEND A REPLY
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}