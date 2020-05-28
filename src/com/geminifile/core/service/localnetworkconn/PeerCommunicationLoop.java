package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;
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

// TODO: STOP THIS LOOP WHEN SERVICE IS STOPPED.
// TODO: MAKE ANOTHER CLASS TO PROCESS THE MESSAGES.
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
                MsgWrapper msg = (MsgWrapper) inStream.readObject();
                System.out.println("[PEER] Received Message Type " + msg.getType().name());

                // Processes the message in a separate thread
                PeerMsgProcessorThread inProcessorThread = new PeerMsgProcessorThread(this, msg);
                inProcessorThread.setName(Thread.currentThread().getName() + "MsgProcessor");
                msgProcessorThreads.add(inProcessorThread); // And puts it into the list.
                inProcessorThread.start();

            } catch (SocketException e) {
                // Means that server disconnects from the other machine.
                outThread.interrupt();
                for (PeerMsgProcessorThread tr : msgProcessorThreads) {
                    tr.interrupt();
                }
                System.out.println("[PEER] Disconnected with: " + node.getIp().getHostAddress());
                break;
            } catch (EOFException e) {
                // Could mean that the server peer has disconnected from this machine.
                outThread.interrupt();
                for (PeerMsgProcessorThread tr : msgProcessorThreads) {
                    tr.interrupt();
                }
                System.out.println("[PEER] Disconnected from: " + node.getIp().getHostAddress());
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("[PEER] Class deserialization error.");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        // The output thread.
        while(true) {
            try {
                MsgWrapper msg = outMessageQueue.take();
                System.out.println("[PEER] Sending Message Type " + msg.getType().name());
                outStream.writeObject(msg);
            } catch (InterruptedException e) {
                // TODO: QUIT OR RESTART WHEN INTERRUPTED.
                return;
            } catch (IOException e) {
                System.out.println("Error writing message to: " + node.getName() + " " + node.getIp().getHostAddress());
                e.printStackTrace();
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
