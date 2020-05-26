package com.geminifile.core.service;

import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

// Abstract super class for LocalClientMsgProcessor and LocalServerMsgProcessor

public abstract class MsgProcessor {

    protected MsgWrapper msg;

    public MsgProcessor(MsgWrapper msg) {
        this.msg = msg;
    }

    public abstract MsgWrapper process();

}