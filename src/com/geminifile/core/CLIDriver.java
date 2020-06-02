package com.geminifile.core;

/*
@author Jason Christian @ https://www.github.com/nakamarusun
*/

import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.fileparser.netfilemanager.NetFileManager;
import com.geminifile.core.service.Service;

import java.util.Map;

public class CLIDriver {
    public static void main(String[] args) {

        if (args.length != 0) {
            // Processes the CLI arguments into a map of arguments and sub-arguments
            Map<String, String> argMap = CLIArgs.argumentProcessor(args);
            // Processes only the first argument, to see what the program is dealing with
            switch (args[0]) {
                case "--help":
                    CLIArgs.viewHelp();
                    break;
                case "-v":
                    CLIArgs.showVersion();
                    break;
                case "start":
                    Service.start();
                    break;
                case "conf":
                    // conf argument
                    break;
                case "log":
                    // log
                    break;
                case "sync":
                    //sync
                    break;
                case "stop":
                    // stop
                    break;
                case "status":
                    CLIArgumentProcessor.status(argMap);
                    break;
                case "refresh":
                    CLIArgumentProcessor.refresh(argMap);
                    break;
                case "json":
                    NetFileManager.start();
                    break;
                default:
                    System.out.println("Unknown command, please refer to 'geminifile --help'");
                    break;
            }
        } else {
            CLIArgs.viewHelp();
        }
    }
}