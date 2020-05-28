package com.geminifile.core.service.localnetworkconn.comms;

import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

public class PeerMsgProcessorThread extends Thread {

    // What loop does this thread belong to.
    PeerCommunicationLoop peer;
    MsgWrapper msg;

    public PeerMsgProcessorThread(PeerCommunicationLoop peer, MsgWrapper msg) {
        this.peer = peer;
        this.msg = msg;
    }

    @Override
    public void run() {
        PeerMsgProcessor msgProcessor = new PeerMsgProcessor(msg);
        MsgWrapper reply = msgProcessor.process();
        if (ExpectingReply.isExpectingReply(msg)) {
            peer.sendMsg(reply);
        }
        // Removes thread from the thread peer List
        peer.removeMsgProcessorThread(this);
    }

}
