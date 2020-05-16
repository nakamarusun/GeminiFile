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
        switch(msg.getType()) {
            case PING:
                return (new MsgWrapper("Pong", MsgType.NOREPLY));
            default:
                return (new MsgWrapper("", MsgType.NOACTION));
        }
    }
}