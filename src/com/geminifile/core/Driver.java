package com.geminifile.core;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

public class Driver {
    public static void main(String[] args) {

        if (args.length != 0) {
            CLIArgs.argumentProcessor(args);
        } else {
            CLIArgs.viewHelp();
        }


    }
} //