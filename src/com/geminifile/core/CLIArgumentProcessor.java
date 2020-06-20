package com.geminifile.core;

import com.geminifile.core.service.localhostconn.LocalClientCommunicator;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.util.Map;
import java.util.Scanner;

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
                    case "binders":
                        msg = new MsgWrapper("binders", MsgType.ASK);
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
                case "f":
                    msg = new MsgWrapper("File", MsgType.COMMAND);
                    break;
            }
        }

        LocalClientCommunicator.sendLocalMessage(msg);
    }

    public static void sync(Map<String, String> args) {

        MsgWrapper msg = new MsgWrapper("", MsgType.NOACTION);

        for (Map.Entry<String, String> e : args.entrySet()) {
            switch (e.getKey()) {
                case "f":
                    msg = new MsgWrapper("FSync", MsgType.COMMAND);
                    break;
                case "d":
                    // Remove whitespaces
                    String binderNames = args.get(e.getKey()).replace(" ", "");
                    msg = new MsgWrapper("SyncFolders-" + binderNames, MsgType.COMMAND);
                    break;
            }
        }

        LocalClientCommunicator.sendLocalMessage(msg);
    }

    public static void binder(Map<String, String> args) {

        MsgWrapper msg = new MsgWrapper("", MsgType.NOACTION);
        Scanner scan = new Scanner(System.in);

        for (Map.Entry<String, String> e : args.entrySet()) {

            StringBuilder binder = new StringBuilder();
            switch (e.getKey()) {
                case "new":

                    System.out.println("Enter new binder details:"); // Adds the binder name
                    System.out.println("\nBinder Name: \n");
                    String name = scan.nextLine();
                    if (name.equals("")) System.out.println("Please enter a valid binder name.");
                    binder.append(name).append(",");

                    System.out.println("\nBinder ID (If empty, will generate random 7 byte alphanumeric): \n"); // Adds the binder id
                    binder.append(scan.nextLine()).append(",");

                    System.out.println("\nBinder path: \n"); // Adds the path
                    binder.append(scan.nextLine()).append(",");

                    msg = new MsgWrapper("NewBinder-" + binder.toString(), MsgType.COMMAND);
                    break;
//                case "del":
//                    // Remove whitespaces
//                    msg = new MsgWrapper("DelBinder-" + e.getKey(), MsgType.COMMAND);
//                    break;
            }
        }

        LocalClientCommunicator.sendLocalMessage(msg);
    }

    public static void stop() {
        LocalClientCommunicator.sendLocalMessage(new MsgWrapper("SHUTDOWN", MsgType.COMMAND));
    }
}
