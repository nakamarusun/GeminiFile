package com.geminifile.core.service.localhostconn;

import com.geminifile.core.service.localhostconn.msgprocessor.LocalClientMsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.geminifile.core.CONSTANTS.LOCALPORT;

public class LocalClientCommunicator {

    private static MsgWrapper msg;

    public static void startLocalClient() {
        System.out.println("[LCLIENT] Connecting to localhost:" + LOCALPORT);
        try {
            // Begin connection
            Socket sock = new Socket("127.0.0.1", LOCALPORT);

            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream()); // note to self: ObjectOutputStream MUST come first, then ObjectInputStream.
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Send the object out to the server.
            localObjectOut.writeObject(msg); // note to self: i think this starts up a NEW THREAD. so, you have to wait for it to be done before closing the socket.

            // if message type is expecting a reply then, wait for localhost server to reply.
            while (ExpectingReply.isExpectingReply(msg)) {
                try {
                    msg = (MsgWrapper) localObjectIn.readObject();
                    System.out.println("[LSERVER] " + msg.toString());
                    // Process message from the server
                    MsgWrapper msgReply = (new LocalClientMsgProcessor(msg)).process(); // Processes the input message
                    if (ExpectingReply.isExpectingReply(msg)) {
                        localObjectOut.writeObject(msgReply);
                    }
                    msg = msgReply;
                } catch (ClassNotFoundException e) {
                    System.out.println("[LCLIENT] Class error not found");
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
            System.out.println("[LCLIENT] No geminifile service is running at localhost:" + LOCALPORT);
        }
    }

    public static void sendLocalMessage(MsgWrapper msg) {
        if (!(msg.getType() == MsgType.NOACTION)) {
            LocalClientCommunicator.msg = msg;
            startLocalClient();
        }
    }

}