package com.geminifile.core.socketmsg;

public enum MsgType {
    CONNQUERY, // Connection query, used for when a client decides to connect to a server
    CONNACCEPT, // The reply to CONNQUERY, with status message (MOTD or smthn)
    INFO, // Asking for information
    COMMAND, // Command (Will not reply)
    PING // Returns pong
}