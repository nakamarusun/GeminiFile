package com.geminifile.core.socketmsg.msgwrapper;

/*
Every message that will be sent to a socket will be first wrapped here.
The function of this class is so that messages has a "type" that gets recognized by the receiver,
and get processed depending on what type of messages they are.
 */

import com.geminifile.core.socketmsg.MsgType;

import java.io.Serializable;

public class MsgWrapper implements Serializable {

    private String content; // Contains a description of a message
    private MsgType type;

    public MsgWrapper(String content, MsgType type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" + type.name() + ": " + content + "}";
    }


}