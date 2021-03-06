package com.geminifile.core.socketmsg.msgwrapper;

import com.geminifile.core.service.Node;
import com.geminifile.core.socketmsg.MsgType;

import java.io.Serializable;

public class MsgIdentification extends MsgWrapper implements Serializable {

    private Node selfNode;

    public MsgIdentification(String content, MsgType type, Node selfNode) {
        super(content, type);
        this.selfNode = selfNode;
    }

    public Node getSelfNode() {
        return selfNode;
    }

    public void setSelfNode(Node selfNode) {
        this.selfNode = selfNode;
    }
}
