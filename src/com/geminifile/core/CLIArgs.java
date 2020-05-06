package com.geminifile.core;

import java.util.regex.*;
import java.util.ArrayList;

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

    public static void commandProcessor(String[] args) {

        // Initialize the variable containing the arguments
        ArrayList<String> arguments = new ArrayList<String>();

        Pattern regSing = Pattern.compile("^(-)[a-zA-Z]{1,100}$"); // Regex for single hyphens
        Pattern regDoub = Pattern.compile("^(--)[a-zA-Z]{1,100}$"); // Regex for double hyphens

        // This for loop is for getting all the arguments from the program.
        for (String str: args) {
            if (regSing.matcher(str).matches()) {
                // If it follows the single hyphen regex
                String newArg = str.substring(1);
                for (Character ch: newArg.toCharArray()) {
                    arguments.add(ch.toString());
                }

            } else if (regDoub.matcher(str).matches()) {
                // If it follows the double hyphen regex
                String newArg = str.substring(2); // Grabs only the word
                arguments.add(newArg);
            }
        }

        for (int i = 0; i < arguments.size(); i++) {
            System.out.println(arguments.get(i));
        }
    }



}
