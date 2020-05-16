package com.geminifile.core.socketmsg;

import java.io.Serializable;

public enum MsgType {
    CONNQUERY, // Connection query, used for when a client decides to connect to a server
    CONNACCEPT, // The reply to CONNQUERY, with status message (MOTD or something)
    ASK, // Asking for information
    INFO, // Give an information (1 means accepted, )
    COMMAND, // Command (returns 1 or 0 depending on done)
    PING, // Returns pong
    NOREPLY, // Does not have to reply to the message.
    NOACTION // Don't reply to the message.
}