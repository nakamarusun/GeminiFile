package com.geminifile.core;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

import java.util.Map;

public class Driver {
    public static void main(String[] args) {

        if (args.length != 0) {
            // Processes the CLI arguments into a map of arguments and sub-arguments
            Map<String, String> argMap = CLIArgs.argumentProcessor(args);
            // Processes only the first argument, to see what the program is dealing with
            switch (argMap.keySet().iterator().next()) {
                case "help":
                    CLIArgs.viewHelp();
                    break;
                case "v":
                    CLIArgs.showVersion();
                    break;
                case "start":
                    // Start argument
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
                    // status
                    break;
                default:
                    System.out.println("Unknown command, please refer to 'geminifile --help'");
                    break;
            }

        } else {
            CLIArgs.viewHelp();
        }


    }
} //