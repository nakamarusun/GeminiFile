package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.MathUtil;
import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.Service;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import static com.geminifile.core.CONSTANTS.BYTESIZE;
import static com.geminifile.core.CONSTANTS.FILEPORT;

public class NetFileSenderThread implements Runnable {

    private final BinderFileDelta binderDelta;
    private final InetAddress ip;

    public NetFileSenderThread(BinderFileDelta binderDelta, InetAddress ip) {
        this.binderDelta = binderDelta;
        this.ip = ip;
    }

    @Override
    public void run() {

        try {
            // Attempts a connection to the designated peer with the FILEPORT port.
            Socket sock = new Socket(ip, FILEPORT);
            Service.LOGGER.info("[NetFile] Starting delta send operation");
            // Define iostream
            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Sets binder file delta to in progress
            binderDelta.setStatus(BinderFileDelta.Status.INPROCESS);

            // Sends the token to verify
            localObjectOut.writeObject(binderDelta.getToken());
            // If the other machine failed to verify it, then the socket will be closed by the other machine.

            Binder binder = BinderManager.getBinder(binderDelta.getId());

            // Sends all of the file
            for (String files : binderDelta.getOtherPeerNeed()) {

                FileInputStream fileStream = null;

                try {
                    // Opens the file and converts it into a binary stream
                    // Opens file
                    String path = binder.getDirectory().getAbsolutePath() + MathUtil.fileSeparatorToOS(files);
                    File file = new File(path); // The current file we are working with

                    if (!file.exists()) {
                        // If file does not exist then, skip
                        Service.LOGGER.severe("[NetFile] Error reading file to send: " + path);
                        continue;
                    }

                    Service.LOGGER.info("[NetFile] Will send file: " + path);

                    // Sends the file metadata to the other peer.
                    NetFile fileMetadata = new NetFile(binderDelta.getToken(), files, file.length());
                    localObjectOut.writeObject(fileMetadata);

                    // Opens file from the File object
                    fileStream = new FileInputStream(file);
                    byte[] bytes = new byte[BYTESIZE];
                    int bytesSendSize = 0; // Bytes read from the current block of the file
                    int byteSent = 0;
                    int blockNum = 0; // current block in operation

                    // Main loop to send files
                    do {
                        if (bytesSendSize > 0) {
                            byteSent += bytesSendSize; // Keeps track of how many bytes have been sent.
                        }

                        bytesSendSize = fileStream.read(bytes, 0, BYTESIZE);

                        NetFileBlock fileBlock;
                        // Checks if file input stream reaches EOF,
                        if (bytesSendSize > 0) {
                            fileBlock = new NetFileBlock(bytes.clone(), bytesSendSize, ++blockNum); // Creates a new file block to send to other machine.
                        } else {
                            byte[] empty = new byte[0]; // Empty byte array
                            fileBlock = new NetFileBlock(empty, 0, 0); // Sends an empty block to the other machine, signifying EOF.
                        }
                        localObjectOut.writeObject(fileBlock); // Sends it to the other machine.
                    } while (bytesSendSize > 0);

                    Service.LOGGER.info("[NetFile] Sent " + fileMetadata.getFileName() + " with " + byteSent + " Bytes in " + blockNum + " block(s)");

//                localObjectOut.writeObject(new NetFile("0", "", 0)); // Sends an EOF prompt
                    fileStream.close(); // Close file input.
                } catch (IOException e) {
                    Service.LOGGER.severe("[NetFile] Error processing file block");
                    Service.LOGGER.log(Level.SEVERE, "exception", e);
                } finally {
                    try {
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException ignored) {}
                }
            }

            // Just wait 1 second before deleting.
            // TODO: Absolute jank. too bad !
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) { }

            // Removes from binder delta operations
            BinderManager.removeBinderDeltaOperation(binderDelta);

            // Closes object IO
            localObjectOut.close();
            localObjectIn.close();
            Service.LOGGER.info("[NetFile] Completed delta send operation token " + binderDelta.getToken());

        } catch (SocketException e) {
            // Means connection suddenly severs
            Service.LOGGER.severe("[NetFile] Failed to complete delta send operation token " + binderDelta.getToken());
        } catch (IOException e) {
            Service.LOGGER.severe("[NetFile] Error opening socket");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        }
    }
}
