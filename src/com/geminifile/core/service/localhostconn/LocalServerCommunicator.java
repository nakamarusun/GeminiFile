package com.geminifile.core.service.localhostconn;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

/*
This class functions as a gateway that receives and send commands from the command line or
application. The received command then could be sent into other parts of the program to run.
This local host can only serve one connection at a time.
 */

public class LocalServerCommunicator implements Runnable {

    private static Thread localServerThread;

    public static void startLocalServer() {

        localServerThread = new Thread(new LocalServerCommunicator(), "LocalhostServerThread");
//        localServerThread.setDaemon(true); // Server is only daemon, when all the main processes is done, this process is optional.
        localServerThread.start();

    }

    public static void debugLocalThreadClient() {
        // tester for client
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Socket sock = new Socket("127.0.0.1", 43743);
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            Scanner scan = new Scanner(System.in);
            while (true) {
                String str = scan.nextLine();
                MsgWrapper msg = new MsgWrapper(str, MsgType.INFO);
                out.writeObject(msg);
                if (str.equals("exit")) { break; }
            }
        } catch (IOException ignored) {}

        System.out.println();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        /*
        This runnable is used to set up a localhost server.
        When the geminifile service starts, it will run in the background with little control of how it runs.
        This module is used to query information and send commands towards that service.
        By setting up a local host server, it can receive and send messages and commands.
         */
        System.out.println("[LSERVER] Opening in localhost:" + LOCALPORT);
        ServerSocket ssock = null;
        try {
            ssock = new ServerSocket(LOCALPORT);
        } catch (IOException e) {
            System.out.println("[LSERVER] Failed to open server socket");
            e.printStackTrace();
            System.exit(5); // Failed to open local server socket
        }

        while (true) {
            // Main loop for accepting new clients
            try {
                Socket sock = ssock.accept(); // Accepts connection
                System.out.println("[LSERVER] Connected !");

                ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());


                while (true) {
                    // Main loop for receiving local message.
                    try {
                        System.out.println("Waiting for message..");
                        MsgWrapper msg = (MsgWrapper) localObjectIn.readObject();
                        System.out.println("[LCLIENT] " + msg.toString());

                        MsgWrapper msgReply = (new LocalServerMsgProcessor(msg)).process(); // Processes the input message

                        // If message type is expecting a reply, then wait for reply from the client.
                        if (ExpectingReply.isExpectingReply(msg)) {
                            localObjectOut.writeObject(msgReply); // Reply message to the client.
                        }

                    } catch (ClassNotFoundException e) {
                        System.out.println("[LSERVER] Class deserialization error");
                        e.printStackTrace();
                    }
                }

            } catch (SocketException ec) {
                System.out.println("[LSERVER] Client Disconnected.");
            } catch (IOException e) {
                System.out.println("[LSERVER] Socket error.");
                e.printStackTrace();
            }
        }
    }
}