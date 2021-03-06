package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.Service;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.geminifile.core.CONSTANTS.TEMPNETFILEPATH;
import static com.geminifile.core.CONSTANTS.TEMPNETFOLDERNAME;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class NetFileReceiverThread implements Runnable {

    private final Socket sock;

    public NetFileReceiverThread(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        Binder binder = null;
        // Defining iostream
        try {

            Service.LOGGER.info("[NetFile] Starting delta receive operation");

            ObjectOutputStream localObjectOut = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream localObjectIn = new ObjectInputStream(sock.getInputStream());

            // Receives a preliminary token check.
            String inToken = (String) localObjectIn.readObject();
            if (!BinderManager.isTokenInBinderDeltas(inToken)) {
                // If the token in question is not in the device token list, then cancel operation.
                Service.LOGGER.severe("[NetFile] Delta operation token " + inToken + " is not registered in machine");
                sock.close();
                return;
            }

            BinderFileDelta binderDelta = BinderManager.getBinderFileDelta(inToken); // Get the binder delta reference
            binder = BinderManager.getBinder(Objects.requireNonNull(binderDelta).getId()); // Get the binder delta reference

            Service.LOGGER.info("[NetFile] Accepted and verified delta file connection from " + sock.getInetAddress().getHostName());

            // Sets binder file delta to in progress
            binderDelta.setStatus(BinderFileDelta.Status.INPROCESS);

            // Creates the temporary binder token folder in the temp folder.
            File tempFolder = new File(TEMPNETFILEPATH + TEMPNETFOLDERNAME + File.separator + "FilesToken-" + inToken);
            tempFolder.mkdir();

            // Continue operations as usual.
            // Receives all of the necessary files to a temporary folder
            for (int i = 0; i < binderDelta.getThisPeerNeed().size(); i++) {

                FileOutputStream fileStream = null;
                try {
                    // Read file metadata
                    NetFile file = (NetFile) localObjectIn.readObject();

//                if (file.getToken().equals("0")) {
//                    // Reaches end of file
//                    break;
//                }

                    String relPathMachine = file.getFilePathName();

                    // Creates the file from the metadata received.
                    String tempFilePath = tempFolder.getAbsolutePath() + relPathMachine;
                    File currentFile = new File(tempFilePath);

                    if (!currentFile.exists()) {
                        // If directory not found up until that point
                        tempFilePath = tempFilePath.substring(0, tempFilePath.lastIndexOf(File.separator));
                        File newFolder = new File(tempFilePath);
                        newFolder.mkdirs(); // Make all of the folders up until that point
                    }

                    Service.LOGGER.info("[NetFile] Expected to receive " + file.getFileName() + " with " + file.getFileSize() + " Bytes");

                    fileStream = new FileOutputStream(currentFile);

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

                    Service.LOGGER.info("[NetFile] Received " + file.getFileName() + " in " + blocks + " block(s) totalling " + byteLength + " Bytes");

                    fileStream.flush(); // Flushes the buffer into the file.
                    fileStream.close(); // Closes fileOutputStream for safety

                    // Copies the files to the main binder folder
                    String destinationFilePath = Objects.requireNonNull(binder).getDirectory().getAbsolutePath() + relPathMachine; // Path destination to copy to.
                    File destinationFile = new File(destinationFilePath);

                    // Checks if the file is there or not to create directories. Then, registers it to the binder's watcher
                    if (!destinationFile.exists()) {

                        // Checks if the directory have been created up until that point
                        destinationFilePath = destinationFilePath.substring(0, destinationFilePath.lastIndexOf(File.separator));
                        File newFolder = new File(destinationFilePath);
                        newFolder.mkdirs(); // Creates directory up until that point

                        // Register the watchers for all of the folders up until that point.
                        String[] folderDepth = relPathMachine.split(Pattern.quote(File.separator));
                        StringBuilder addPath = new StringBuilder();

                        for (int j = 0; j < (folderDepth.length - 1); j++) {
                            addPath.append(folderDepth[j]).append(File.separator);
                            if (folderDepth[j].equals("")) continue; // If its an empty string, then don't bother.
                            binder.registerSubWatcherService(new File(binder.getDirectory().getAbsolutePath() + File.separator + addPath));
                        }
                    }

                    // Try atomic move
                    binder.setFileToIgnore(destinationFile.toPath().toString());
                    try {
                        Files.move(currentFile.toPath(), destinationFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                    } catch (AtomicMoveNotSupportedException | AccessDeniedException e) {
                        try {
                            Files.move(currentFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (FileSystemException ex) {
                            Service.LOGGER.severe("[NetFile] Moving file from temporary folder failed");
                            Service.LOGGER.log(Level.SEVERE, "exception", ex);
                        }
                    }
                } catch (IOException e) {
                    Service.LOGGER.severe("[NetFile] Error file");
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
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) { binder.setFileToIgnore(""); }

            // Removes from binder delta operations
            BinderManager.removeBinderDeltaOperation(binderDelta);

            // delete temp folder
            tempFolder.delete(); // TODO: Not working
            localObjectIn.close();
            localObjectOut.close();
            binder.setFileToIgnore("");
            Service.LOGGER.info("[NetFile] Completed delta receive operation token " + inToken);

        } catch (SocketException | EOFException e) {
            Service.LOGGER.warning("[NetFile] Delta file connection disconnected from " + sock.getInetAddress().getHostName());
        } catch (ClassNotFoundException e) {
            Service.LOGGER.severe("[NetFile] Class deserialization error.");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        } catch (NullPointerException e) {
            Service.LOGGER.severe("[NetFile] Binder file delta is not found in the database.");
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        } catch (IOException e) {
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        } finally {
            assert binder != null;
            binder.setFileToIgnore("");
        }
    }

}
