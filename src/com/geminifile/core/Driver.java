package com.geminifile.core;

import com.geminifile.core.CLIArgs;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

public class Driver {
    public static void main(String[] args) {

        if (args.length != 0) {
            CLIArgs.commandProcessor(args);
        } else {
            CLIArgs.viewHelp();
        }


    }
}