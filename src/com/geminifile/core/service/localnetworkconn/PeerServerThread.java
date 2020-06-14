package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.Node;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgIdentification;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import static com.geminifile.core.CONSTANTS.COMMPORT;

// This thread first function as a handshake with the other peer, sending the query first to the other peer.
// After successfully verified,
// TODO: WHEN CONNECTING TO OTHERS, THAT OTHER CAN CONNECT HERE AS WELL. FIX THAT ! MAYBE USING LOCKS AND STUFF
public class PeerServerThread implements Runnable, OnConnectOperation {

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

            try {
                PeerCommunicatorManager.lockPeerConnectionLock();

                // Attempts a query
                localObjectOut.writeObject(new MsgIdentification("query", MsgType.CONNQUERY, Service.getMyNode()));

                // Receives a reply
                MsgIdentification replyQuery = (MsgIdentification) localObjectIn.readObject();

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
            } finally {
                PeerCommunicatorManager.unlockPeerConnectionLock();
            }

            // At this point, the device has successfully handshake and established connection with the other device
            Service.LOGGER.severe("[PEER] Successfully established connection with " + sock.getInetAddress().getHostAddress());

            PeerCommunicationLoop commsLoop = new PeerCommunicationLoop(sock, otherNode, localObjectIn, localObjectOut);
            PeerCommunicatorManager.addPeerTable(commsLoop);

            // Before starting the comms thread, do preliminary script
            newConnectionMade(commsLoop);

            // Starts up the comms
            commsLoop.startComms();
        } catch (EOFException ignored) {

        } catch (ClassNotFoundException e) {
            Service.LOGGER.severe("[PEER] Class deserialization error");
            Service.LOGGER.log(Level.SEVERE, "exception", e);

        } catch (SocketException e) {
            // If an io exception occurs, usually because of a connection reset, quit the thread.
            Service.LOGGER.warning("[PEER] Disconnected from " + nodeIp.getHostAddress());
            PeerCommunicatorManager.removePeerTable(otherNode);

        } catch (IOException e) {
            Service.LOGGER.severe("[PEER] IOException occurred");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        }

        // For safety, remove when thread is done.
        PeerCommunicatorManager.removePeerTable(otherNode);

    }

    @Override
    public void newConnectionMade(PeerCommunicationLoop peer) {
        // Do stuff when this peer connects to other peer after query.
        // Usually preliminary stuff
        // Ask the other device if binders is in sync
        Service.LOGGER.info("[FILE] Sent AskBinderHave to " + nodeIp.getHostAddress());
        peer.sendMsg(BinderManager.getAskBinderHave());
    }
}
