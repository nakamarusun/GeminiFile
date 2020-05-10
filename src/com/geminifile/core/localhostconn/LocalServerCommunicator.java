package com.geminifile.core.localhostconn;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.MsgWrapper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

/*
This class functions as a gateway that receives and send commands from the command line or
application. The received command then could be sent into other parts of the program to run.
This local host can only serve one connection at a time.
 */

public class LocalServerCommunicator {

    private static Thread localServerThread;
    private static SynchronousQueue<MsgWrapper> inboundMsg;

    public static void startLocalServer() {

        inboundMsg = new SynchronousQueue<MsgWrapper>(true);

        localServerThread = new Thread(new LocalServerThread());
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
        } catch (IOException e) {}

        System.out.println("");
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
}