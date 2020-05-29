package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;
import org.json.JSONObject;

public class PeerMsgProcessor extends MsgProcessor implements ExpectingReply {

    public PeerMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {

        MsgWrapper msgProc = (new MsgWrapper("", MsgType.NOACTION));

        switch (msg.getType()) {
            case ASK:
                if (msg.getContent().startsWith("AskBinderHave-")) {
                    // Will process a message with content: "AskBinderHave-id1-id2-id3-id4"
                    // Will process it and return a statement: "AskBinderReply-{id1:[files have]}-{id3:[files have]}"
                    // Asks if this peer has binders with the corresponding id
                    String[] ids = msg.getContent().substring(msg.getContent().indexOf("-") + 1).split("-");
                    StringBuilder messageContent = new StringBuilder();

                    // Iterate the ids sent, and based if this device has the corresponding id, and reply with a JSONObject
                    // with their own File listing. Ignore if don't have
                    for (String e : ids) {

                        Binder currentBinder = BinderManager.getBinder(e);
                        if (currentBinder != null) {
                            // Do something if have
                            JSONObject reply = new JSONObject(); // JSONObject to return file listing.
                            // Gets all the file directory with their corresponding lastModified.
                            reply.put(e, currentBinder.getFileListing()); // Puts the file listing into the reply object
                            messageContent.append(reply.toString()).append("-"); // appends to the StringBuilder
                        }

                    }

                    msgProc = new MsgWrapper("AskBinderReply-" + messageContent.toString(), MsgType.INFO);
                    break;
                } else if (msg.getContent().startsWith("AskBinderReply-")) {
                    // Will process a message with content: "AskBinderReply-{id1:[files have]}-{id3:[files have]}"
                    // Do file Syncing
                    // Processes what the current device have, and doesn't have.

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
