package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.MathUtil;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static com.geminifile.core.CONSTANTS.FILEPORT;

public class NetFileSenderThread implements Runnable {

    private final BinderFileDelta fileDelta;
    private final InetAddress ip;

    public NetFileSenderThread(BinderFileDelta fileDelta, InetAddress ip) {
        this.fileDelta = fileDelta;
        this.ip = ip;
    }

    @Override
    public void run() {

        try {
            // Attempts a connection to the designated peer with the FILEPORT port.
            Socket sock = new Socket(ip, FILEPORT);

            // Define iostream
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Sends the token to verify
            localObjectOut.writeObject(fileDelta.getToken());
            // If the other machine failed to verify it, then the socket will be closed by the other machine.

            // Sends all of the file
            for (String files : fileDelta.getOtherPeerNeed()) {
                // Opens the file and converts it into a binary stream
                // TODO: Split into blocks if you have time.

                // Opens file
                String path = BinderManager.getBinder(fileDelta.getId()).getDirectory().getAbsolutePath() + MathUtil.fileSeparatorToOS(files);
                File file = new File(path);

                System.out.println("[NetFile] Will send file: " + path);

                if (!file.exists()) {
                    System.out.println("[NetFile] Error reading file to send: " + path);
                }


            }

        } catch (IOException e) {
            System.out.println("[NetFile] Error opening socket");
            e.printStackTrace();
        }



    }
}
