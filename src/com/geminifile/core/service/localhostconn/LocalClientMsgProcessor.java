package com.geminifile.core.service.localhostconn;

import com.geminifile.core.service.LocalMsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

public class LocalClientMsgProcessor extends LocalMsgProcessor implements ExpectingReply {

    public LocalClientMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {

        MsgWrapper noAction = (new MsgWrapper("", MsgType.NOACTION));

        switch(msg.getType()) {
            case ASK:
                switch (msg.getContent()) {
                    case "Done":
                        return (new MsgWrapper("Status received successfully.", MsgType.INFO));
                    default:
                        return noAction;
                }
            default:
                return noAction;
        }
    }
}
