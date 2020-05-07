package com.geminifile.core.localhostconn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import static com.geminifile.core.CONSTANTS.LOCALPORT;

// UNENCRYPTED LOCALHOST SERVER COMMAND RECEIVER

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
                System.out.println(in.readLine()); // Everytime the client flushes, the message gets printed
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}