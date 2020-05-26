package com.geminifile.core.service.localhostconn.msgprocessor;

/*
This class takes an MsgWrapper object, and processes it depending on the content, then
returns a corresponding reply MsgWrapper object.
 */

import com.geminifile.core.service.LocalMsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

import java.util.Set;

public class LocalServerMsgProcessor extends LocalMsgProcessor implements ExpectingReply {


    public LocalServerMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {
        // Processes the message in the constructor, and returns a MsgWrapper reply object.
        // The program can also return a NOREPLY MsgWrapper object to signify not to reply anything.

        MsgWrapper noAction = (new MsgWrapper("", MsgType.NOACTION));

        switch(msg.getType()) {
            case ASK:
                switch (msg.getContent()) {
                    case "status":
                        // TODO: INFORMATIVE STATUS
                        return (new MsgWrapper("All ok !", MsgType.INFO));
                    case "threads":
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();
                        StringBuilder strBuild = new StringBuilder();
                        strBuild.append("Current running threads on GeminiFile Service:\n");
                        for (Thread t : threads) {
                            strBuild.append(String.format("%-25s \t %s \t %-2d %s\n",
                                    t.getName(), t.getState(), t.getPriority(), t.isDaemon() ? "Daemon" : "Normal"));
                        }
                        return (new MsgWrapper(strBuild.toString(), MsgType.INFO));
                    default:
                        return noAction;
                }
            case PING:
                return (new MsgWrapper("Pong", MsgType.INFO));
            case INFO:
                System.out.println(msg.toString());
                return noAction;
            case COMMAND:
                // TODO: DOES NOT WORK PROPERLY
                if (msg.getContent().equals("RefNet")) {
                    Service.restartNetworkingService();
                }
                return (new MsgWrapper("Done", MsgType.ASK));
            default:
                return noAction;
        }
    }

}