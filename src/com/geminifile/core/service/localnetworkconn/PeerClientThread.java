package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

// This thread can also handle ping queries.
// TODO: DO SOME SOCKET CLOSES
// TODO: REMOVE FROM PEER TABLE
public class PeerClientThread implements Runnable {

    private final Socket sock;
    private Node otherNode;

    public PeerClientThread(Socket mySocket) {
        this.sock = mySocket;
    }

    @Override
    public void run() {
        try {
            // Defining iostream
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Accept query from the peer. Whether this is a query or not it is decided later.
            MsgIdentification inQuery = (MsgIdentification)localObjectIn.readObject();
            // Handle ping and exit. If not, then continue as usual
            if (inQuery.getContent().equals("ping")) {
                localObjectOut.writeObject(new MsgIdentification("pinggood", MsgType.CONNACCEPT, Service.getMyNode()));
                return;
            } else if ( !(inQuery.getContent().equals("query") && inQuery.getType() == MsgType.CONNQUERY)) {
                // if message is not ping and not query then quit the thread
                return;
            }
            // TODO: CHECK IN TRUSTED DEVICES FILE WHETHER PEER IS KNOWN. IF IT IS NOT KNOWN, PROMPT THE USER TO TRUST IT OR NOT.
            // Send self id as CONNQUERY accept
            localObjectOut.writeObject(new MsgIdentification("replyquery", MsgType.CONNQUERY, Service.getMyNode()));

            // If the program has reached this point, preliminary peer identification is complete, and accept an OK request
            MsgWrapper okMsg = (MsgWrapper)localObjectIn.readObject();

            // If connection is accepted and message is right then add to peerTable
            if (okMsg.getType() == MsgType.CONNACCEPT && okMsg.getContent().equals("allok")) {
                otherNode = inQuery.getSelfNode();
            } else {
                // Exit thread
                sock.close();
                return;
            }

            PeerCommunicatorManager.addPeerTable(otherNode, sock);

            System.out.println("Successfully accepted connection from " + sock.getInetAddress().getHostAddress());
            while (true) {
                // Main message manager loop
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Class deserialization error");
            e.printStackTrace();

        } catch (SocketException e) {
            // Stops the socket.
            System.out.println("Disconnected from " + sock.getInetAddress().getHostAddress());
            PeerCommunicatorManager.removePeerTable(otherNode); // TODO: may cause some problems when removing while iterating @ PeerCommunicatorManager

        } catch (IOException e) {
            System.out.println("IOException occurred");
            e.printStackTrace();

        }

        // For safety, remove when thread is done.
        PeerCommunicatorManager.removePeerTable(otherNode);
    }

}
