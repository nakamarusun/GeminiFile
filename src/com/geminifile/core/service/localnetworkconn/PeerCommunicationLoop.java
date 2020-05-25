package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

// TODO: STOP THIS LOOP WHEN SERVICE IS STOPPED.
public class PeerCommunicationLoop implements Runnable {

    private final Socket sock;
    private final Node node;
    private final ObjectInputStream inStream;
    private final ObjectOutputStream outStream;

    private final ArrayBlockingQueue<MsgWrapper> outMessageQueue;
    private final ArrayBlockingQueue<MsgWrapper> inMessageQueue;

    public PeerCommunicationLoop(Socket sock, Node node, ObjectInputStream inStream, ObjectOutputStream outStream) {
        this.sock = sock;
        this.node = node;
        this.inStream = inStream;
        this.outStream = outStream;

        // Maximum message queue is 10.
        outMessageQueue = new ArrayBlockingQueue<>(10, true);
        inMessageQueue = new ArrayBlockingQueue<>(10, true);
    }

    public void startComms() {
        // The current thread becomes the input thread, a new thread is created for the output thread.
        Thread outThread = new Thread(this, (Thread.currentThread().getName() + "OutStream")); // Renames the threads to get a better information
        Thread.currentThread().setName(Thread.currentThread().getName() + "InStream");

        // Starts the thread
        outThread.start();

        // THIS IS THE MAIN INPUT LOOP, PROCESS INPUT
        while (true) {
            // Waits until there is an prompt to send.
            try {
                MsgWrapper msg = (MsgWrapper)inStream.readObject();
                // Puts it into the inMessageQueue
                inMessageQueue.offer(msg);
            } catch (ClassNotFoundException e) {
                System.out.println("Class deserialization error.");
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
                outStream.writeObject(msg);
            } catch (InterruptedException e) {
                // TODO: QUIT OR RESTART WHEN INTERRUPTED.
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
}
