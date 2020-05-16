package com.geminifile.core.localhostconn;

import com.geminifile.core.socketmsg.MsgProcessor;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

public class LocalClientCommunicator {

    private static MsgWrapper msg;

    public static void startLocalClient() {
        System.out.println("Connecting to localhost:" + LOCALPORT);
        try {
            // Begin connection
            Socket sock = new Socket("127.0.0.1", LOCALPORT);

            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream()); // note to self: ObjectOutputStream MUST come first, then ObjectInputStream.
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            localObjectOut.writeObject(msg); // note to self: i think this starts up a NEW THREAD. so, you have to wait for it to be done before closing the socket.

            // if message type is expecting a reply then, wait for localhost server to reply.
            if (MsgProcessor.isExpectingReply(msg)) {
                try {
                    MsgWrapper msg = (MsgWrapper) localObjectIn.readObject();
                    System.out.println("[SERVER] " + msg.toString());
                } catch (ClassNotFoundException e) {
                    System.out.println("Class error not found");
                    e.printStackTrace();
                }
            }

            // Waits for all operations to be done.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            System.out.println("No geminifile service is running at localhost:" + LOCALPORT);
            e.printStackTrace();
        }
    }

    public static void sendLocalMessage(MsgWrapper msg) {
        LocalClientCommunicator.msg = msg;
        startLocalClient();
    }

}