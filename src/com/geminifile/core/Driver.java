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
            System.out.println(argMap.toString());

        } else {
            CLIArgs.viewHelp();
        }


    }
} //