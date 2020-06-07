package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
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

            BinderFileDelta binder = BinderManager.getBinderFileDelta(inToken);

            System.out.println("[NetFile] Accepted and verified delta file connection from " + sock.getInetAddress().getHostName());

            // Creates the temporary binder token folder in the temp folder.
            File tempFolder = new File(TEMPNETFILEPATH + TEMPNETFOLDERNAME + File.separator + "FilesToken-" + inToken);
            tempFolder.mkdir();

            // Continue operations as usual.
            // Receives all of the necessary files to a temporary folder
            for (int i = 0; i < binder.getThisPeerNeed().size(); i++) {
                // Read file metadata
                NetFile file = (NetFile) localObjectIn.readObject();

//                if (file.getToken().equals("0")) {
//                    // Reaches end of file
//                    break;
//                }

                // Creates the file from the metadata received.
                File currentFile = new File(tempFolder.getAbsolutePath() + File.separator + file.getFilePathName());
                FileOutputStream fileStream = new FileOutputStream(currentFile);

                int blocks = 0; // Blocks of NetFileBlock received
                long byteLength = 0; // bytes of file received.

                // Downloads the file in blocks
                while (true) {
                    NetFileBlock fileBlock = (NetFileBlock) localObjectIn.readObject();
                    // If fileBlock has reached the end, then stop process.
                    if (fileBlock.getSize() <= 0) {
                        break;
                    }
                    fileStream.write(fileBlock.getBlock(), 0, fileBlock.getSize());
                    byteLength += fileBlock.getSize();
                    blocks++;
                }

                System.out.println("[NetFile] Expected to receive " + file.getFileName() + " with " + file.getFileSize() + " Bytes");
                System.out.println("[NetFile] Received " + file.getFileName() + " in " + blocks + " block(s) totalling " + byteLength + " Bytes");

                fileStream.flush(); // Flushes the buffer into the file.
                fileStream.close(); // Closes fileOutputStream for safety

            }

            // delete temp folder
//            tempFolder.delete();
            localObjectIn.close();
            localObjectOut.close();
            System.out.println("[NetFile] Completed operation token " + inToken);

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
