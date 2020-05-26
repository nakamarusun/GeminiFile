package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

public class PeerMsgProcessor extends MsgProcessor implements ExpectingReply {

    public PeerMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {

        MsgWrapper noAction = (new MsgWrapper("", MsgType.NOACTION));
        MsgWrapper msgProc;

        switch (msg.getType()) {

            default:
                msgProc = noAction;
        }

        return msgProc;
    }
}
