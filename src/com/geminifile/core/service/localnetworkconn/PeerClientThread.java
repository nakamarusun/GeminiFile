package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.socketmsg.msgwrapper.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerClientThread implements Runnable {

    Socket sock;

    public PeerClientThread(Socket mySocket) {
        this.sock = mySocket;
    }

    @Override
    public void run() {
        try {
            // Defining iostream
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            try {
                // Accept query from the peer.
                MsgIdentification inQuery = (MsgIdentification)localObjectIn.readObject();


            } catch (ClassNotFoundException e) {
                System.out.println("Class deserialization error");
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}
