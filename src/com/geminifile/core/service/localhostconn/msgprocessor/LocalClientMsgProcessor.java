package com.geminifile.core.service.localhostconn.msgprocessor;

import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

public class LocalClientMsgProcessor extends MsgProcessor implements ExpectingReply {

    public LocalClientMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {

        MsgWrapper msgProc = new MsgWrapper("", MsgType.NOACTION);

        MsgWrapper noAction = (new MsgWrapper("", MsgType.NOACTION));

        switch(msg.getType()) {
            case ASK:
                switch (msg.getContent()) {
                    case "Done":
                        msgProc = new MsgWrapper("Status received successfully.", MsgType.INFO);
                }
        }
        return msgProc;
    }
}
