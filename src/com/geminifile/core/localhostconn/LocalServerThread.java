package com.geminifile.core.localhostconn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
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
    private BufferedReader in;

    @Override
    public void run() {
        try {
            System.out.println("Opening port in " + LOCALPORT);
            ssock = new ServerSocket(LOCALPORT);

            sock = ssock.accept(); // Accepts connection
            System.out.println("Connected !");
            in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Starts a new string parser from byte data

            while(true) {
                System.out.println(in.readLine()); // Every time the client flushes, the message gets printed
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}