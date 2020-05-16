package com.geminifile.core.socketmsg;

import java.io.Serializable;

public enum MsgType {
    CONNQUERY, // Connection query, used for when a client decides to connect to a server
    CONNACCEPT, // The reply to CONNQUERY, with status message (MOTD or smthn)
    ASK, // Asking for information
    INFO, // Give an information (1 means accepted, )
    COMMAND, // Command (Will not reply)
    PING, // Returns pong
    NOREPLY // Does not have to reply to the message.
}