package com.geminifile.core.service.localhostconn;

/*
This class takes an MsgWrapper object, and processes it depending on the content, then
returns a corresponding reply MsgWrapper object.
 */

import com.geminifile.core.service.LocalMsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

public class LocalServerMsgProcessor extends LocalMsgProcessor implements ExpectingReply {


    public LocalServerMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
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
            case COMMAND:
                if (msg.getContent().equals("RefNet")) {
                    Service.restartNetworkingService();
                }
                return (new MsgWrapper("Done", MsgType.ASK));
            default:
                return noAction;
        }
    }

}