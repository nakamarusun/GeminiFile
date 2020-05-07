package com.geminifile.core.localhostconn;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import static com.geminifile.core.CONSTANTS.LOCALPORT;
import com.geminifile.core.socketmsg.*;

/*
This class is used to set up a localhost server.
When the geminifile service starts, it will run in the background with little control of how it runs.
This module is used to query information and send commands towards that service.
By setting up a local host server, it can receive and send messages and commands.
 */

public class LocalServerThread implements Runnable {

    private ServerSocket ssock;
    private Socket sock;
    private BufferedReader in;

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
                    System.out.println(msg.toString());
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}