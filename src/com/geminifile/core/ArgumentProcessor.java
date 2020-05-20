package com.geminifile.core;

import com.geminifile.core.service.localhostconn.LocalClientCommunicator;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.util.Map;

/* Processes arguments based on the function they are calling.
this might seem can be better coded, but this is actually for
The convenience to sort arguments based on their functions
to make things tidier.
 */

public class ArgumentProcessor {

    public static void status(Map<String, String> args) {
        LocalClientCommunicator.sendLocalMessage(new MsgWrapper("status", MsgType.ASK));
    }

    public static void refresh(Map<String, String> args) {
        for (Map.Entry<String, String> e : args.entrySet()) {
            if (e.getKey().equals("n")) {
                LocalClientCommunicator.sendLocalMessage(new MsgWrapper("RefNet", MsgType.COMMAND));
            }
        }
    }

}
