package com.geminifile.core.localhostconn;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.MsgWrapper;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

//

public class LocalServerCommunicator {

    public static void startLocalServer() {
        Thread t = new Thread(new LocalServerThread());
        t.setDaemon(true);
        t.start();

        // tester for client
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Socket sock = new Socket("127.0.0.1", 43743);
//            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
//            Scanner scan = new Scanner(System.in);
//            while (true) {
//                String str = scan.nextLine();
//                MsgWrapper msg = new MsgWrapper(str, MsgType.INFO);
//                out.writeObject(msg);
//                if (str.equals("exit")) { break; }
//            }
//        } catch (IOException e) {}
//
//        System.out.println("");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}