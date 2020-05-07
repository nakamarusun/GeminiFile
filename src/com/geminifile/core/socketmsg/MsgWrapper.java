package com.geminifile.core.socketmsg;

/*
Every message that will be sent to a socket will be first wrapped here.
The function of this class is so that messages has a "type" that gets recognized by the receiver,
and get processed depending on what type of messages they are.
 */

public class MsgWrapper {

    String content;
    MsgType type;

}