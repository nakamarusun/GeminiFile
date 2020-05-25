package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgIdentification;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import static com.geminifile.core.CONSTANTS.COMMPORT;

// TODO: DO SOME SOCKET CLOSES
public class PeerServerThread implements Runnable {

    private InetAddress nodeIp;
    private Node otherNode;

    public PeerServerThread(InetAddress nodeIp) {
        this.nodeIp = nodeIp;
    }

    @Override
    public void run() {

        try {
            // Attempts a connection
            Socket sock = new Socket(nodeIp, COMMPORT);

            // Opens a object iostream
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Attempts a query
            localObjectOut.writeObject(new MsgIdentification("query", MsgType.CONNQUERY, Service.getMyNode()));

            // Receives a reply
            MsgIdentification replyQuery = (MsgIdentification)localObjectIn.readObject();

            // Checks the reply
            if (!(replyQuery.getContent().equals("replyquery") && replyQuery.getType() == MsgType.CONNQUERY)) {
                // Condition does not meet, exits the thread.
                sock.close();
                return;
            }

            // If the program has reached this point, preliminary peer identification is complete, and send OK message
            localObjectOut.writeObject(new MsgWrapper("allok", MsgType.CONNACCEPT));
            // Puts id into peerTable
            otherNode = replyQuery.getSelfNode();

            // At this point, the device has successfully handshake and established connection with the other device
            System.out.println("Successfully established connection with " + sock.getInetAddress().getHostAddress());

            PeerCommunicationLoop commsLoop = new PeerCommunicationLoop(sock, otherNode, localObjectIn, localObjectOut);
            PeerCommunicatorManager.addPeerTable(commsLoop);
            commsLoop.startComms();

        } catch (ClassNotFoundException e) {
            System.out.println("Class deserialization error");
            e.printStackTrace();

        } catch (SocketException e) {
            // If an io exception occurs, usually because of a connection reset, quit the thread.
            System.out.println("Disconnected from " + nodeIp.getHostAddress());
            PeerCommunicatorManager.removePeerTable(otherNode);

        } catch (IOException e) {
            System.out.println("IOException occurred");
            e.printStackTrace();
        }

        // For safety, remove when thread is done.
        PeerCommunicatorManager.removePeerTable(otherNode);

    }

}
