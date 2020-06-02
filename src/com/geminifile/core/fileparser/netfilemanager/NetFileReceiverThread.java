package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.fileparser.binder.BinderManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static com.geminifile.core.CONSTANTS.*;

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

            System.out.println("[NetFile] Accepted and verified delta file connection from " + sock.getInetAddress().getHostName());

            // Creates the temporary binder token folder in the temp folder.
            File tempFolder = new File(TEMPNETFILEPATH + TEMPNETFOLDERNAME + File.separator + "FilesToken-" + inToken);
            tempFolder.mkdir();

            // Continue operations as usual.
            // Receives all of the necessary files to a temporary folder
            while (true) {
                NetFile file = (NetFile) localObjectIn.readObject();
                if (file.getToken().equals("0")) {
                    // Reaches end of file
                    break;
                }

                // Puts the file into a temporary folder


            }

            // delete temp folder
            tempFolder.delete();
        } catch (SocketException | EOFException e) {
            System.out.println("[NetFile] Delta file connection disconnected from " + sock.getInetAddress().getHostName());
        } catch (ClassNotFoundException e) {
            System.out.println("[NetFile] Class deserialization error.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
