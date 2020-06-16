package com.geminifile.core.service.localhostconn;

import com.geminifile.core.service.Service;
import com.geminifile.core.service.localhostconn.msgprocessor.LocalServerMsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

/*
This class functions as a gateway that receives and send commands from the command line or
application. The received command then could be sent into other parts of the program to run.
This local host can only serve one connection at a time.
 */

public class LocalServerCommunicator implements Runnable {

    private static Thread localServerThread;
    private static ServerSocket ssock;

    public static void startLocalServer() {

        localServerThread = new Thread(new LocalServerCommunicator(), "LocalhostMessageServer");
//        localServerThread.setDaemon(true); // Server is only daemon, when all the main processes is done, this process is optional.
        localServerThread.start();

    }

    public static void debugLocalThreadClient() {
        // tester for client
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Service.LOGGER.log(Level.SEVERE, "exception", e);
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
            Service.LOGGER.log(Level.SEVERE, "exception", e);
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
        Service.LOGGER.info("[LSERVER] Opening in localhost:" + LOCALPORT);
        ssock = null;
        try {
            ssock = new ServerSocket(LOCALPORT);
        } catch (IOException e) {
            Service.LOGGER.severe("[LSERVER] Failed to open server socket");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
            System.exit(5); // Failed to open local server socket
        }

        while (true) {
            // Main loop for accepting new clients
            try {
                Socket sock = ssock.accept(); // Accepts connection
                Service.LOGGER.info("[LSERVER] Connected !");

                ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());


                while (true) {
                    // Main loop for receiving local message.
                    try {
                        MsgWrapper msg = (MsgWrapper) localObjectIn.readObject();
                        Service.LOGGER.info("[LCLIENT] " + msg.toString());

                        MsgWrapper msgReply = (new LocalServerMsgProcessor(msg)).process(); // Processes the input message

                        // If message type is expecting a reply, then wait for reply from the client.
                        if (ExpectingReply.isExpectingReply(msg)) {
                            localObjectOut.writeObject(msgReply); // Reply message to the client.
                        }

                    } catch (ClassNotFoundException e) {
                        Service.LOGGER.severe("[LSERVER] Class deserialization error");
                        Service.LOGGER.log(Level.SEVERE, "exception", e);
                    }
                }

            } catch (SocketException | EOFException ec) {
                Service.LOGGER.severe("[LSERVER] Client Disconnected.");
                if (localServerThread.isInterrupted()) break; // breaks from loop when service is stopped.
            } catch (IOException e) {
                Service.LOGGER.severe("[LSERVER] Socket error.");
                Service.LOGGER.log(Level.SEVERE, "exception", e);
            } catch (Exception e) {
                Service.LOGGER.log(Level.SEVERE, "exception", e);
                break;
            }
        }
    }

    public static void stopService() {
        try {
            ssock.close(); // Closes the service
        } catch (IOException e) {
            Service.LOGGER.severe("[LSERVER] Error closing serversocket.");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        }
        localServerThread.interrupt(); // And interrupts the thread.
    }
}