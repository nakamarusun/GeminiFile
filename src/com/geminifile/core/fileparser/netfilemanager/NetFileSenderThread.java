package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.fileparser.binder.BinderFileDelta;

import java.net.InetAddress;

public class NetFileSenderThread implements Runnable {

    private BinderFileDelta fileDelta;
    private InetAddress ip;

    public NetFileSenderThread(BinderFileDelta fileDelta, InetAddress ip) {
        this.fileDelta = fileDelta;
        this.ip = ip;
    }

    @Override
    public void run() {

    }
}
