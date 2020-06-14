package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;
import com.geminifile.core.service.Service;
import com.geminifile.core.service.localnetworkconn.comms.PeerMsgProcessorThread;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

// TODO: STOP THIS LOOP WHEN SERVICE IS STOPPED.
public class PeerCommunicationLoop implements Runnable {

    private final Socket sock;
    private final Node node;
    private final ObjectInputStream inStream;
    private final ObjectOutputStream outStream;

    private final ArrayBlockingQueue<MsgWrapper> outMessageQueue;

    private final List<PeerMsgProcessorThread> msgProcessorThreads = new ArrayList<>();

    public PeerCommunicationLoop(Socket sock, Node node, ObjectInputStream inStream, ObjectOutputStream outStream) {
        this.sock = sock;
        this.node = node;
        this.inStream = inStream;
        this.outStream = outStream;

        // Maximum message queue is 10.
        outMessageQueue = new ArrayBlockingQueue<>(10, true);
    }

    public void startComms() {

        String threadBeginning = Thread.currentThread().getName();

        // The current thread becomes the input thread, a new thread is created for the output thread.
        Thread outThread = new Thread(this, (Thread.currentThread().getName() + "OutStream")); // Renames the threads to get a better information

        Thread.currentThread().setName(Thread.currentThread().getName() + "InStream");

        // Starts the thread
        outThread.start();


        // THIS IS THE MAIN INPUT LOOP, PROCESS INPUT
        while (true) {
            // Waits until there is an prompt to send.
            try {
                Object inObj = inStream.readObject();

                MsgWrapper msg = (MsgWrapper) inObj;

                Service.LOGGER.info("[PEER] Received Message Type " + msg.getType().name());

                // Processes the message in a separate thread
                PeerMsgProcessorThread inProcessorThread = new PeerMsgProcessorThread(this, msg);
                inProcessorThread.setName(Thread.currentThread().getName() + "-MsgProcessor-" + msg.getType().name());
                msgProcessorThreads.add(inProcessorThread); // And puts it into the list.
                inProcessorThread.start();

            } catch (SocketException e) {
                // Means that server disconnects from the other machine.
                outThread.interrupt();
                for (PeerMsgProcessorThread tr : msgProcessorThreads) {
                    tr.interrupt();
                }
                Service.LOGGER.warning("[PEER] Disconnected with: " + node.getIp().getHostAddress());
                // Tries to close the IO Stream
                try {
                    inStream.close();
                    outStream.close();
                } catch (IOException ex) {
                    Service.LOGGER.severe("[PEER] Failed to close communication io stream");
                    Service.LOGGER.log(Level.SEVERE, "exception", e);
                }
                break;
            } catch (EOFException e) {
                // Could mean that the server peer has disconnected from this machine.
                outThread.interrupt();
                for (PeerMsgProcessorThread tr : msgProcessorThreads) {
                    tr.interrupt();
                }
                Service.LOGGER.warning("[PEER] Disconnected from: " + node.getIp().getHostAddress());
                // Tries to close the IO Stream
                try {
                    inStream.close();
                    outStream.close();
                } catch (IOException ex) {
                    Service.LOGGER.severe("[PEER] Failed to close communication io stream");
                    Service.LOGGER.log(Level.SEVERE, "exception", e);
                }
                break;
            } catch (ClassNotFoundException e) {
                Service.LOGGER.severe("[PEER] Class deserialization error.");
                Service.LOGGER.log(Level.SEVERE, "exception", e);
            } catch (IOException e) {
                Service.LOGGER.log(Level.SEVERE, "exception", e);
            }
        }
    }

    @Override
    public void run() {
        // The output thread.
        while(true) {
            try {
                MsgWrapper msg = outMessageQueue.take();
                Service.LOGGER.info("[PEER] Sending Message Type " + msg.getType().name());
                outStream.writeObject(msg);
            } catch (InterruptedException e) {
                // TODO: QUIT OR RESTART WHEN INTERRUPTED.
                return;
            } catch (IOException e) {
                Service.LOGGER.info("Error writing message to: " + node.getName() + " " + node.getIp().getHostAddress());
                Service.LOGGER.log(Level.SEVERE, "exception", e);
            }
        }

    }

    public Socket getSock() {
        return sock;
    }

    public Node getNode() {
        return node;
    }

    public boolean sendMsg(MsgWrapper msg) {
        return outMessageQueue.offer(msg);
    }

    public void removeMsgProcessorThread(PeerMsgProcessorThread thread) {
        msgProcessorThreads.remove(thread);
    }

}
