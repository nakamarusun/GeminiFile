package com.geminifile.core.socketmsg.msgwrapper;

/*
Every message that will be sent to a socket will be first wrapped here.
The function of this class is so that messages has a "type" that gets recognized by the receiver,
and get processed depending on what type of messages they are.
Update 11 June 2020: Added byte array so it can contain another object's data
 */

import com.geminifile.core.socketmsg.MsgType;

import java.io.Serializable;

public class MsgWrapper implements Serializable {

    private String content; // Contains a description of a message
    private MsgType type;
    private Byte[] byteData; // Contains the byte data

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

    public Byte[] getByteData() {
        return byteData;
    }

    public void setByteData(Byte[] byteData) {
        this.byteData = byteData;
    }

    @Override
    public String toString() {
        return "{" + type.name() + ": " + content + " with " + (byteData != null ? byteData.length : 0) + " bytes in data.}";
    }


}