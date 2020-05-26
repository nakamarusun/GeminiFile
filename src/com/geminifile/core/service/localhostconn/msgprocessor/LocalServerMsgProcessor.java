package com.geminifile.core.service.localhostconn.msgprocessor;

/*
This class takes an MsgWrapper object, and processes it depending on the content, then
returns a corresponding reply MsgWrapper object.
 */

import com.geminifile.core.service.ActivePeerGetter;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

import java.util.Set;

public class LocalServerMsgProcessor extends MsgProcessor implements ExpectingReply {


    public LocalServerMsgProcessor(MsgWrapper msg) {
        super(msg);
    }

    @Override
    public MsgWrapper process() {
        // Processes the message in the constructor, and returns a MsgWrapper reply object.
        // The program can also return a NOREPLY MsgWrapper object to signify not to reply anything.

        MsgWrapper msgProc = new MsgWrapper("", MsgType.NOACTION);

        switch(msg.getType()) {
            case ASK:
                switch (msg.getContent()) {
                    case "status":
                        // TODO: INFORMATIVE STATUS
                        msgProc = new MsgWrapper("All ok !", MsgType.INFO);
                        break;
                    case "threads":
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();
                        StringBuilder strBuild = new StringBuilder();
                        strBuild.append("Current running threads on GeminiFile Service:\n");
                        for (Thread t : threads) {
                            strBuild.append(String.format("%-25s \t %s \t %-2d %s\n",
                                    t.getName(), t.getState(), t.getPriority(), t.isDaemon() ? "Daemon" : "Normal"));
                        }
                        msgProc = new MsgWrapper(strBuild.toString(), MsgType.INFO);
                        break;
                }
                break;
            case PING:
                msgProc = new MsgWrapper("Pong", MsgType.INFO);
                break;
            case INFO:
                System.out.println(msg.toString());
                break;
            case COMMAND:
                // TODO: DOES NOT WORK PROPERLY
                switch (msg.getContent()) {
                    case "RefNet":
                        Service.restartNetworkingService();
                        break;
                    case "Ping":
                        ActivePeerGetter.restartService();
                        break;
                }
                msgProc = new MsgWrapper("Done", MsgType.ASK);
                break;
        }

        return msgProc;
    }

}