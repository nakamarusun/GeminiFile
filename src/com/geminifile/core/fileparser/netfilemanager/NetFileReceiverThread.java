package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.fileparser.binder.BinderManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetFileReceiverThread implements Runnable {

    private Socket sock;

    public NetFileReceiverThread(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        // Defining iostream
        try {
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Receives a preliminary token check.
            String inToken = (String) localObjectIn.readObject();
            if (!BinderManager.isTokenInBinderDeltas(inToken)) {
                // If the token in question is not in the device token list, then cancel operation.
                sock.close();
                return;
            }

            // Continue operations as usual.

        } catch (ClassNotFoundException e) {
            System.out.println("[NetFile] Class deserialization error.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
