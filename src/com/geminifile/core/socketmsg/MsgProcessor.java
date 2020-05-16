package com.geminifile.core.socketmsg;

/*
This class takes an MsgWrapper object, and processes it depending on the content, then
returns a corresponding reply MsgWrapper object.
 */

import com.geminifile.core.socketmsg.msgwrapper.*;

public class MsgProcessor {

    MsgWrapper msg;

    public MsgProcessor(MsgWrapper msg) {
        this.msg = msg;
    }

    public MsgWrapper process() {
        // Processes the message in the constructor, and returns a MsgWrapper reply object.
        // The program can also return a NOREPLY MsgWrapper object to signify not to reply anything.

        MsgWrapper noAction = (new MsgWrapper("", MsgType.NOACTION));

        switch(msg.getType()) {
            case ASK:
                switch (msg.getContent()) {
                    case "status":
                        return (new MsgWrapper("All ok !", MsgType.INFO));
                    default:
                        return noAction;
                }
            case PING:
                return (new MsgWrapper("Pong", MsgType.INFO));
            case INFO:
                System.out.println(msg.toString());
                return noAction;
            default:
                return noAction;
        }
    }

    public static boolean isExpectingReply(MsgWrapper msg) {
        return msg.getType() == MsgType.CONNQUERY
                || msg.getType() == MsgType.ASK
                || msg.getType() == MsgType.COMMAND
                || msg.getType() == MsgType.PING; // Returns true if the MstType is listed here
    }

}