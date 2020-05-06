package com.geminifile.core;

public class CLIArgs {

    // TODO: complete --help and use regex (or .contains) for CLI command recognition. Make into a function

    public static void viewHelp() {
        System.out.println("usage: geminifile [--help] \n\n" +
                "Common geminifile commands:\n\n" +
                "geminifile start: Starts server based on configuration file (Starts localhost on port default 39494)\n" +
                "geminifile conf: view configuration file in CLI\n" +
                "   [-l <all local port>], [-s <sync port>]\n" +
                "geminifile log: view update log and stuff\n" +
                "geminifile sync: force sync of files\n" +
                "   [-d <folder to sync>] [-f force sync]\n" +
                "geminifile stop: stops service\n" +
                "geminifile status: status of the server, folder locations to sync\n" +
                "");
    }



}
