package com.geminifile.core.localhostconn;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

/*
This class functions as a gateway that receives and send commands from the command line or
application. The received command then could be sent into other parts of the program to run.
This local host can only serve one connection at a time.
 */

public class LocalServerCommunicator implements Runnable {

    private static Thread localServerThread;
    private static SynchronousQueue<MsgWrapper> inboundMsg;


    public static void startLocalServer() {

        inboundMsg = new SynchronousQueue<MsgWrapper>(true);

        localServerThread = new Thread(new LocalServerCommunicator());
        localServerThread.setDaemon(true); // Server is only daemon, when all the main processes is done, this process is optional.
        localServerThread.start();

    }

    private static void processMsg(MsgWrapper msg) {

    }

    private static void debugLocalThreadClient() {
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

    public static void putMessage(MsgWrapper msg) {
        try {
            inboundMsg.put(msg);
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
        try {
            // TODO: MAKE SERVER REQUIRE AUTHENTICATION BEFORE CONNECTING

            System.out.println("Opening port in " + LOCALPORT);
            ServerSocket ssock = new ServerSocket(LOCALPORT);

            Socket sock = ssock.accept(); // Accepts connection
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