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
public class CLIArgumentProcessor {

    public static void status(Map<String, String> args) {

        MsgWrapper msg = new MsgWrapper("", MsgType.NOACTION);

        if (args.size() != 0) {
            // Iterate within the arguments
            for (Map.Entry<String, String> e : args.entrySet()) {
                switch (e.getKey()) {
                    case "threads":
                        msg = new MsgWrapper("threads", MsgType.ASK);
                        break;
                    case "p":
                        msg = new MsgWrapper("peers", MsgType.ASK);
                        break;
                    case "n":
                        msg = new MsgWrapper("MyNode", MsgType.ASK);
                        break;
                    case "f":
                        msg = new MsgWrapper("DeltaOperations", MsgType.ASK);
                        break;
                    default:
                        msg = new MsgWrapper("", MsgType.NOACTION);
                }
            }
        } else {
            msg = new MsgWrapper("status", MsgType.ASK);
        }

        LocalClientCommunicator.sendLocalMessage(msg);
    }

    public static void refresh(Map<String, String> args) {

        MsgWrapper msg = new MsgWrapper("", MsgType.NOACTION);

        for (Map.Entry<String, String> e : args.entrySet()) {
            switch (e.getKey()) {
                case "n":
                    msg = new MsgWrapper("RefNet", MsgType.COMMAND);
                    break;
                case "p":
                    msg = new MsgWrapper("Ping", MsgType.COMMAND);
                    break;
            }
        }

        LocalClientCommunicator.sendLocalMessage(msg);
    }

}
