package com.geminifile;

/*
By Jason Christian @ https://www.github.com/nakamarusun
*/

public class Driver {
    public static void main(String[] args) {

        // TODO: complete --help and use regex (or .contains) for CLI command recognition. Make into a function
        if (args.length != 0) {
            switch (args[0]) {
                case "--help":
                    System.out.println("usage: geminifile [--help]");
                    break;
                default:
                    System.out.println("Unknown command, please refer to 'geminifile --help'");
                    break;
            }
        }

    }
}