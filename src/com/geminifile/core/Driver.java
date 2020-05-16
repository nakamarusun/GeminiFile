package com.geminifile.core;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

import com.geminifile.core.localhostconn.LocalClientCommunicator;
import com.geminifile.core.localhostconn.LocalServerCommunicator;
import com.geminifile.core.service.Service;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;

import java.util.Map;

public class Driver {
    public static void main(String[] args) {

        if (args.length != 0) {
            // Processes the CLI arguments into a map of arguments and sub-arguments
            Map<String, String> argMap = CLIArgs.argumentProcessor(args);
            // Processes only the first argument, to see what the program is dealing with
            switch (argMap.keySet().iterator().next()) {
                case "--help":
                    CLIArgs.viewHelp();
                    break;
                case "-v":
                    CLIArgs.showVersion();
                    break;
                case "start":
                    LocalServerCommunicator.startLocalServer();
                    Service.start();
                    break;
                case "conf":
                    // conf argument
                    break;
                case "log":
                    // log
                    break;
                case "sync":
                    // sync
                    break;
                case "stop":
                    // stop
                    break;
                case "status":
                    LocalClientCommunicator.sendLocalMessage(new MsgWrapper("status", MsgType.ASK));
                    break;
                default:
                    System.out.println("Unknown command, please refer to 'geminifile --help'");
                    break;
            }
        } else {
            // TODO: Starts the application GUI
        }
    }
}