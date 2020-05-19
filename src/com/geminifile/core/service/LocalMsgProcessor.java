package com.geminifile.core.service;

import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

// Abstract super class for LocalClientMsgProcessor and LocalServerMsgProcessor

public abstract class LocalMsgProcessor {

    protected MsgWrapper msg;

    public LocalMsgProcessor(MsgWrapper msg) {
        this.msg = msg;
    }

    public abstract MsgWrapper process();

}