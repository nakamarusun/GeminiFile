package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.fileparser.netfilemanager.NetFileSenderThread;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

// TODO: Add locks to prevent file from being accessed.
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
                    // Will process it and return a statement: "AskBinderReply-{id1:[files have], id3:[files have]}"
                    // Asks if this peer has binders with the corresponding id
                    String[] ids = contentWithoutStart.split("-");
                    JSONObject reply = new JSONObject(); // JSONObject to return file listing.

                    // Iterate the ids sent, and based if this device has the corresponding id, and reply with a JSONObject
                    // with their own File listing. Ignore if don't have
                    for (String e : ids) {

                        Binder currentBinder = BinderManager.getBinder(e);
                        if (currentBinder != null) {
                            // Do something if have
                            // Gets all the file directory with their corresponding lastModified.
                            reply.put(e, currentBinder.getFileListing()); // Puts the file listing into the reply object
                        }

                    }

                    msgProc = new MsgWrapper("AskBinderReply-" + reply.toString(), MsgType.ASK);
                    break;
                } else if (msg.getContent().startsWith("AskBinderReply-")) {
                    // Will process a message with content: "AskBinderReply-{id1:[files have], id3:[files have]}"
                    // Will process it and return a statement: "AskBinderFileList-{"otherPeerNeed":[files], "id":id, "thisPeerNeed":[files], "token":token}-.......
                    // Do file Syncing
                    // Processes what the current device have, and doesn't have.
                    JSONObject otherJsonFileBinder = new JSONObject(contentWithoutStart); // The json format of the received message
                    StringBuilder finalString = new StringBuilder();
                    for (String e : otherJsonFileBinder.keySet()) { // e is every id in the json object.
                        // Loops within the binder ids that the other machine has, and create a new list that
                        // Differentiates the files that other machine has and this.
                        HashMap<String, Long> otherFileListing = new HashMap<>();

                        // Iterates within the id to get all the fileListing
                        JSONObject otherJsonFileListing = otherJsonFileBinder.getJSONObject(e);
                        for (String file : otherJsonFileListing.keySet()) {
                            // Puts into HashMap
                            otherFileListing.put(file, otherJsonFileListing.getLong(file));
                        }

                        try {
                            // Make a new entry in the binder delta operation based on the current files with the other machine's file.
                            BinderFileDelta fileDelta = new BinderFileDelta(e, communicatedPeer, Objects.requireNonNull(BinderManager.getBinder(e)).getFileListing(), otherFileListing);
                            JSONObject switchedBinderDeltaJSON = fileDelta.getSwitchedBinderDeltaJSON(); // Switch the delta to send to the other device.

                            BinderManager.addBinderDeltaOperation(fileDelta);

                            // Replies with what files does the other device need from this device.
                            finalString.append(switchedBinderDeltaJSON.toString()).append("-");

                        } catch (NullPointerException ex) {
                            // If failed to get the current binder from the binder manager
                            ex.printStackTrace();
                        }
                    }

                    msgProc = new MsgWrapper("AskBinderFileList-" + finalString.toString(), MsgType.ASK);

                } else if (msg.getContent().startsWith("AskBinderFileList-")) {
                    // Will process a message with content: "AskBinderFileList-{"otherPeerNeed":[files], "id":id, "thisPeerNeed":[files], "token":token}-.......
                    // Will process it and return a statement: "AskBinderConfirmed-token1-token2-
                    // Puts the JSON object to the the list of BinderDeltas in BinderManager

                    StringBuilder finalString = new StringBuilder();

                    String[] receivedJSON = contentWithoutStart.split("-");
                    for (String e : receivedJSON) {

                        JSONObject fileDeltaJSON = new JSONObject(e);
                        BinderFileDelta fileDelta = new BinderFileDelta(fileDeltaJSON);
                        BinderManager.addBinderDeltaOperation(fileDelta);

                        finalString.append(fileDelta.getToken()).append("-");

                        // Verify the message, and the file availability in the system, open the File port, and sends the files to the other device.
                        // Start connection thread here.
                        Thread fileSenderThread = new Thread(new NetFileSenderThread(fileDelta, communicatedPeer.getSock().getInetAddress()));
                        fileSenderThread.start();
                        System.out.println("[Should be in linux] " + fileDelta.getBinderDeltaJSON().toString());
                    }

                    msgProc = new MsgWrapper("AskBinderConfirmed-" + finalString.toString(), MsgType.ASK);
                    System.out.println(msgProc.toString());

                } else if (msg.getContent().startsWith("AskBinderConfirmed-")) {

                    // Starts Binder delta operation based on received tokens..
                    String[] receivedTokens = contentWithoutStart.split("-");
                    for (String e : receivedTokens) {

                        BinderFileDelta fileDelta = BinderManager.getBinderFileDelta(e);

                        // Start connection thread here.
                        Thread fileSenderThread = new Thread(new NetFileSenderThread(fileDelta, communicatedPeer.getSock().getInetAddress()));
                        fileSenderThread.start();
                        System.out.println("[Should be in celine] " + fileDelta.getBinderDeltaJSON().toString());
                    }
                }
                break;
            case INFO:
                System.out.println(msg.toString());
                break;
        }

        return msgProc;
    }
}
