package com.geminifile.core.service.localhostconn;

import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

public interface ExpectingReply {
    // Interface for Processor classes to know whether a message needs a reply or not.
    public static boolean isExpectingReply(MsgWrapper msg) {
        return msg.getType() == MsgType.CONNQUERY
                || msg.getType() == MsgType.ASK
                || msg.getType() == MsgType.COMMAND
                || msg.getType() == MsgType.PING; // Returns true if the MstType is listed here
    }
}
