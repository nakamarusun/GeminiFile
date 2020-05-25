package com.geminifile.core.service.localnetworkconn;

import com.geminifile.core.service.Node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerCommunicationLoop implements Runnable {

    private Socket sock;
    private Node node;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;

    public PeerCommunicationLoop(Socket sock, Node node, ObjectInputStream inStream, ObjectOutputStream outStream) {
        this.sock = sock;
        this.node = node;
        this.inStream = inStream;
        this.outStream = outStream;
    }

    public void startComms() {
        // The current thread becomes the input thread, a new thread is created for the output thread.
        Thread outThread = new Thread(this, (Thread.currentThread().getName() + "OutStream")); // Renames the threads to get a better information
        Thread.currentThread().setName(Thread.currentThread().getName() + "InStream");

        // Starts the thread
        outThread.start();

        while (true) {
            // Waits until there is an prompt to send.
        }

    }

    @Override
    public void run() {
        // The output thread.

    }

    public Socket getSock() {
        return sock;
    }

    public Node getNode() {
        return node;
    }
}
