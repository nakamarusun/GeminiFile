package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// This thread can also handle ping queries.

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
                // Accept query from the peer. Whether this is a query or not it is decided later.
                MsgIdentification inQuery = (MsgIdentification)localObjectIn.readObject();
                // Handle ping and exit. If not, then continue as usual
                if (inQuery.getContent().equals("ping")) {
                    localObjectOut.writeObject(new MsgIdentification("pinggood", MsgType.CONNACCEPT, Service.getMyNode()));
                    return;
                } else if ( !inQuery.getContent().equals("query")) {
                    // TODO: CHECK IN TRUSTED DEVICES FILE WHETHER PEER IS KNOWN. IF IT IS NOT KNOWN, PROMPT THE USER TO TRUST IT OR NOT.
                    // if message is not ping and not query then quit the thread
                    return;
                }
                // Send self id as CONNQUERY accept
                localObjectOut.writeObject(new MsgIdentification("reply", MsgType.CONNQUERY, Service.getMyNode()));
                // Accept an OK request
                MsgWrapper okMsg = (MsgWrapper)localObjectIn.readObject();

                // If connection is accepted and message is right then add to peerTable
                if (okMsg.getType() == MsgType.CONNACCEPT && okMsg.getContent().equals("OKClient")) {
                    PeerCommunicatorManager.addPeerTable(inQuery.getSelfNode());
                } else {
                    // Exit thread
                    return;
                }

                while (true) {
                    // Main message manager loop
                }

            } catch (ClassNotFoundException e) {
                System.out.println("Class deserialization error");
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}
