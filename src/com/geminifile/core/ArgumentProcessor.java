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
// TODO: Make this more efficient
public class ArgumentProcessor {

    public static void status(Map<String, String> args) {
        if (args.size() != 0) {
            // Iterate within the arguments
            for (Map.Entry<String, String> e : args.entrySet()) {
                MsgWrapper msg;
                switch (e.getKey()) {
                    case "threads":
                        msg = new MsgWrapper("threads", MsgType.ASK);
                        break;
                    default:
                        msg = new MsgWrapper("", MsgType.NOACTION);
                }
                LocalClientCommunicator.sendLocalMessage(msg);
            }
        } else {
            LocalClientCommunicator.sendLocalMessage(new MsgWrapper("status", MsgType.ASK));
        }
    }

    public static void refresh(Map<String, String> args) {
        // TODO: FIX THIS
        for (Map.Entry<String, String> e : args.entrySet()) {
            if (e.getKey().equals("n")) {
                LocalClientCommunicator.sendLocalMessage(new MsgWrapper("RefNet", MsgType.COMMAND));
            }
        }
    }

}
