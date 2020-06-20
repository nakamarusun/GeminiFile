package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.fileparser.binder.FileListing;
import com.geminifile.core.fileparser.netfilemanager.NetFileSenderThread;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerMsgProcessor extends MsgProcessor implements ExpectingReply {

    PeerCommunicationLoop communicatedPeer; // To get a reference of the peer the message is dealing with.

    public PeerMsgProcessor(MsgWrapper msg, PeerCommunicationLoop peer) {
        super(msg);
        communicatedPeer = peer;
    }

    @Override
    public MsgWrapper process() {

        MsgWrapper msgProc = (new MsgWrapper("", MsgType.NOACTION));

        String contentWithoutStart = msg.getContent().contains("-") ? msg.getContent().substring(msg.getContent().indexOf("-") + 1) : msg.getContent();

        switch (msg.getType()) {
            case ASK:
                if (msg.getContent().startsWith("AskBinderHave-")) {
                    // IMPORTANT: USE THIS ONLY FOR BEGINNING WHEN CONNECTION IS MADE. BECAUSE THE FILE CHECKING FUNCTION IS KIND OF HEAVY.
                    // Will process a message with content: "AskBinderHave-id1-id2-id3-id4"
                    // Will process it and return a statement: "AskBinderReply-{id1:[{relPath:x, lastModified:y, checkSum:z}, ..], id3:[{relPath:x, lastModified:y, checkSum:z}, ..]}"
                    // Asks if this peer has binders with the corresponding id
                    String[] ids = contentWithoutStart.split("-");
                    JSONObject reply = new JSONObject(); // JSONObject to return file listing.

                    // Iterate the ids sent, and based if this device has the corresponding id, and reply with a JSONObject
                    // with their own File listing. Ignore if don't have
                    boolean haveBinder = false; // If the machine does not have the binder in question, then don't send a reply
                    for (String e : ids) {

                        Binder currentBinder = BinderManager.getBinder(e);
                        if (currentBinder != null) {
                            // Do something if have
                            // Gets all the file directory with their corresponding lastModified.
                            List<FileListing> fileListing = currentBinder.getFileListing();
                            JSONArray fileListingJSONArr = new JSONArray(); // The array of file listings
                            for (FileListing file : fileListing) {
                                JSONObject fileListingJSON = new JSONObject();
                                fileListingJSON.put("relPath", file.getRelativePath());
                                fileListingJSON.put("lastModified", file.getLastModified());
                                fileListingJSON.put("checkSum", file.getCheckSumMD5());
                                fileListingJSONArr.put(fileListingJSON);
                            }
                            reply.put(e, fileListingJSONArr); // Puts the file listing into the reply object
                            haveBinder = true;
                        }

                    }
                    if (haveBinder) {
                        msgProc = new MsgWrapper("AskBinderReply-" + reply.toString(), MsgType.ASK);
                    }
                    break;
                } else if (msg.getContent().startsWith("AskBinderReply-")) {
                    // Will process a message with content: "AskBinderReply-{id1:[{relPath:x, lastModified:y, checkSum:z}, ..], id3:[{relPath:x, lastModified:y, checkSum:z}, ..]}"
                    // Will process it and return a statement: "AskBinderFileList-{"otherPeerNeed":[files], "id":id, "thisPeerNeed":[files], "token":token}-.......
                    // Do file Syncing
                    // Processes what the current device have, and doesn't have.

                    boolean noDelta = false; // If there is no delta in the files

                    JSONObject otherJsonFileBinder = new JSONObject(contentWithoutStart); // The json format of the received message
                    StringBuilder finalString = new StringBuilder();
                    for (String e : otherJsonFileBinder.keySet()) { // e is every id in the json object.
                        // Loops within the binder ids that the other machine has, and create a new list that
                        // Differentiates the files that other machine has and this.
                        ArrayList<FileListing> otherFileListing = new ArrayList<>();

                        // Iterates within the id to get all the fileListing
                        JSONArray otherJsonFileListing = otherJsonFileBinder.getJSONArray(e);
                        for (int i = 0; i < otherJsonFileListing.length(); i++) {
                            // Puts into list
                            JSONObject currentFileJSON = otherJsonFileListing.getJSONObject(i);
                            otherFileListing.add(new FileListing(
                                    currentFileJSON.getString("relPath"),
                                    currentFileJSON.getLong("lastModified"),
                                    currentFileJSON.getString("checkSum")
                            ));
                        }

                        try {
                            // This block is to check whether another AskBinderReply- query is in this machine with the same binder.
                            // TODO: Check if there is already an operation inside of deltaOperations with the same binder.
                            boolean cancelThisBinder = false; // If binder is cancelled, then proceed to the next of binder operation
                            Binder binderRef = Objects.requireNonNull(BinderManager.getBinder(e));
                            for (BinderFileDelta delta : BinderManager.getAllBinderFileDelta()) {
                                // If there is a binder of the same id in the delta operation.
                                if (delta.getId().equals(binderRef.getId())) {
                                    Service.LOGGER.info("[Binder] Binder " + binderRef.getName() + " is currently in operation. Cancelling AskBinderReply..." );
                                    cancelThisBinder = true;
                                    break;
                                }
                            }
                            if (cancelThisBinder) {
                                continue;
                            }

                            // Make a new entry in the binder delta operation based on the current files with the other machine's file.
                            // (e, communicatedPeer, Objects.requireNonNull(BinderManager.getBinder(e)).getFileListing(), otherFileListing)
                            BinderFileDelta fileDelta = new BinderFileDelta(e, communicatedPeer, binderRef.getFileListing(), otherFileListing);
                            if (fileDelta.getThisPeerNeed().size() == 0 && fileDelta.getOtherPeerNeed().size() == 0) {
                                // If the file delta operations are empty, then do not send a message
                                noDelta = true;
                                break;
                            }

                            JSONObject switchedBinderDeltaJSON = fileDelta.getSwitchedBinderDeltaJSON(); // Switch the delta to send to the other device.

                            BinderManager.addBinderDeltaOperation(fileDelta);

                            // Replies with what files does the other device need from this device.
                            finalString.append(switchedBinderDeltaJSON.toString()).append("-");

                        } catch (NullPointerException ex) {
                            // If failed to get the current binder from the binder manager
                            Service.LOGGER.log(Level.SEVERE, "exception", ex);
                        }
                    }

                    if (!noDelta) {
                        msgProc = new MsgWrapper("AskBinderFileList-" + finalString.toString(), MsgType.ASK);
                    }

                } else if (msg.getContent().startsWith("AskBinderFileList-")) {
                    // Will process a message with content: "AskBinderFileList-{"otherPeerNeed":[files], "id":id, "thisPeerNeed":[files], "token":token}-.......
                    // Will process it and return a statement: "AskBinderConfirmed-token1-token2-
                    // Puts the JSON object to the the list of BinderDeltas in BinderManager

                    StringBuilder finalString = new StringBuilder();

                    if (contentWithoutStart.length() == 0) return msgProc; // If the contents is empty.

                    String[] receivedJSON = contentWithoutStart.split("-");
                    for (String e : receivedJSON) {

                        Service.LOGGER.severe(e);
                        JSONObject fileDeltaJSON = new JSONObject(e);
                        BinderFileDelta fileDelta = new BinderFileDelta(fileDeltaJSON);

                        BinderManager.addBinderDeltaOperation(fileDelta);

                        finalString.append(fileDelta.getToken()).append("-");

                        // Verify the message, and the file availability in the system, open the File port, and sends the files to the other device.
                        // Start connection thread here.
                        if (fileDelta.getOtherPeerNeed().size() != 0) { // If there is no file needed by the other device, then don't bother to start the process
                            Thread fileSenderThread = new Thread(new NetFileSenderThread(fileDelta, communicatedPeer.getSock().getInetAddress()), "FileSender-" + fileDelta.getToken());
                            fileSenderThread.start();
                        } else {
                            BinderManager.removeBinderDeltaOperation(fileDelta);
                        }
                    }
                    Service.LOGGER.severe(finalString.toString());

                    msgProc = new MsgWrapper("AskBinderConfirmed-" + finalString.toString(), MsgType.ASK);

                } else if (msg.getContent().startsWith("AskBinderConfirmed-")) {

                    // Starts Binder delta operation based on received tokens..
                    String[] receivedTokens = contentWithoutStart.split("-");
                    for (String e : receivedTokens) {

                        BinderFileDelta fileDelta = BinderManager.getBinderFileDelta(e);

                        // Start connection thread here.
                        assert fileDelta != null;
                        if (fileDelta.getOtherPeerNeed().size() != 0) { // If there is no file needed by the other device, then don't bother to start the process
                            Thread fileSenderThread = new Thread(new NetFileSenderThread(fileDelta, communicatedPeer.getSock().getInetAddress()), "FileSender-" + fileDelta.getToken());
                            fileSenderThread.start();
                        } else {
                            BinderManager.removeBinderDeltaOperation(fileDelta);
                        }
                    }
                } else if (msg.getContent().startsWith("WantToSync-")) {
                    // Will return the list of binders that this machine have in common.
                    String reply;
                    String id = contentWithoutStart.split(":")[0];
                    String token = contentWithoutStart.split(":")[1];
                    int filesToReceive = Integer.parseInt(contentWithoutStart.split(":")[2]);
                    if (BinderManager.getBinder(id) != null) {
                        BinderFileDelta delta = new BinderFileDelta(id, token);
                        for (int i = 0; i < filesToReceive; i++) {
                            delta.addThisPeedNeed(""); // Bogus
                        }
                        BinderManager.addBinderDeltaOperation(delta);
                        reply = "T:" + token;
                    } else {
                        reply = "F";
                    }

                    msgProc = new MsgWrapper("WantToSyncHave-" + reply, MsgType.ASK);

                } else if (msg.getContent().startsWith("WantToSyncHave-")) {
                    if (contentWithoutStart.startsWith("T:")) {
                        BinderFileDelta fileDelta = BinderManager.getBinderFileDelta( contentWithoutStart.substring(contentWithoutStart.indexOf(":") + 1) );
                        assert fileDelta != null;
                        Thread fileSenderThread = new Thread(new NetFileSenderThread(fileDelta, communicatedPeer.getSock().getInetAddress()), "FileSender-" + fileDelta.getToken());
                        fileSenderThread.start();
                    }
                }
                break;
            case INFO:
                Service.LOGGER.info(msg.toString());
                break;
        }

        return msgProc;
    }
}
