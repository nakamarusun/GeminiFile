package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;
import org.json.JSONObject;

import java.util.HashMap;

public class PeerMsgProcessor extends MsgProcessor implements ExpectingReply {

    public PeerMsgProcessor(MsgWrapper msg) {
        super(msg);
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
                    // Do file Syncing
                    // Processes what the current device have, and doesn't have.
                    JSONObject otherJsonFileBinder = new JSONObject(contentWithoutStart); // The json format of the received message
                    for (String e : otherJsonFileBinder.keySet()) {
                        // Loops within the binder ids that the other machine has, and create a new list that
                        // Differentiates the files that other machine has and this.
                        HashMap<String, Long> otherFileListing = new HashMap<>();

                        // Iterates within the id to get all the fileListing
                        JSONObject otherJsonFileListing = new JSONObject(otherJsonFileBinder.getString(e));
                        for (String file : otherJsonFileListing.keySet()) {
                            // Puts into HashMap
                            otherFileListing.put(file, otherJsonFileListing.getLong(file));
                        }
                        // Do comparison with the current binder's id file listing.

                    }

                    // Replies with what files does the other device need from this device.
                }
                break;
            case INFO:
                System.out.println(msg.toString());
                break;
        }

        return msgProc;
    }
}
