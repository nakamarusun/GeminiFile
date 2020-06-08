package com.geminifile.core.service.localhostconn.msgprocessor;

/*
This class takes an MsgWrapper object, and processes it depending on the content, then
returns a corresponding reply MsgWrapper object.
 */

import com.geminifile.core.fileparser.binder.BinderFileDelta;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.PingerManager;
import com.geminifile.core.service.MsgProcessor;
import com.geminifile.core.service.Service;
import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager;
import com.geminifile.core.socketmsg.ExpectingReply;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.*;

import java.util.Set;

import static com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager.getPeerTable;

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
                StringBuilder str;
                switch (msg.getContent()) {
                    case "status":
                        // TODO: INFORMATIVE STATUS
                        msgProc = new MsgWrapper("All ok !", MsgType.INFO);
                        break;
                    case "threads":
                        // Shows all of the threads
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();
                        str = new StringBuilder();
                        str.append("Current running threads on GeminiFile Service:\n");
                        for (Thread t : threads) {
                            str.append(String.format("%-25s \t %s \t %-2d %s\n",
                                    t.getName(), t.getState(), t.getPriority(), t.isDaemon() ? "Daemon" : "Normal"));
                        }
                        msgProc = new MsgWrapper(str.toString(), MsgType.INFO);
                        break;
                    case "peers":
                        // Shows all of the peer connected
                        str = new StringBuilder("\nIP:Port               |Name            |ID          |OS\n");
                        for (PeerCommunicationLoop e : PeerCommunicatorManager.getPeerTable()) {
                            str.append(String.format("%-22s|%-16s|%-12s|%s\n",
                                    e.getSock().getInetAddress().getHostAddress() + ":" + e.getSock().getPort(),
                                    e.getNode().getName(),
                                    e.getNode().getId().substring(0, 8) + "..",
                                    e.getNode().getOs()));
                        }
                        msgProc = new MsgWrapper(str.toString(), MsgType.INFO);
                        break;
                    case "DeltaOperations":
                        // Shows all the current delta operations
                        str = new StringBuilder("\nDeltaToken  |BinderID    |OtherPeerID |Status\n");
                        for (BinderFileDelta e : BinderManager.getAllBinderFileDelta()) {
                            str.append(String.format("\n%-12s|%-12s|%-12s|%s\n",
                                    e.getToken(),
                                    e.getId() + "..",
                                    e.getPeerNodeId() + "..",
                                    e.getStatus().name()));
                            str.append("This peer needs: ").append(e.getThisPeerNeed().toString()).append("\n");
                            str.append("Other peer needs: ").append(e.getOtherPeerNeed().toString()).append("\n\n");
                        }
                        msgProc = new MsgWrapper(str.toString(), MsgType.INFO);
                        break;
                    case "MyNode":
                        msgProc = new MsgWrapper(
                                "\n" + Service.getMyNode().toString() + "\n",
                                MsgType.INFO
                        );
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
                        PingerManager.restartService();
                        break;
                }
                msgProc = new MsgWrapper("Done", MsgType.ASK);
                break;
        }

        return msgProc;
    }

}