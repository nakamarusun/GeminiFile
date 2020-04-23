package com.geminifile;

public class Driver {
    public static void main(String[] args) {

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