package com.geminifile.core;

import com.geminifile.core.CLIArgs;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

public class Driver {
    public static void main(String[] args) {

        if (args.length != 0) {
            switch (args[0]) {
                case "--help":
                    CLIArgs.viewHelp();
                    break;
                default:
                    System.out.println("Unknown command, please refer to 'geminifile --help'");
                    break;
            }
        }


    }
}