package com.geminifile.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CLIArgs {


    public static void viewHelp() {
        System.out.println("usage: geminifile [--help] [-v version]\n\n" +
                "Common geminifile commands:\n\n" +

                "geminifile start: Starts server based on configuration file (Starts localhost on port default 39494)\n" +

                "geminifile sync: force sync of files\n" +
                "   [-d <binder name> syncs folder with ',' delimiter] [-f force sync]\n" +

                "geminifile stop: stops service\n" +

                "geminifile binder: does nothing on its own. Sets a new status of a binder.\n" +
                "   [--new registers a new binder to the main service.]\n" +

                "geminifile status: status of the server, folder locations to sync\n" +
                "   [--threads views all current threads running] [-p returns all active peers] [-n shows current node] [-f in-queue file delta operations]\n" +
                "   [--binders all of the binders in this device]\n" +

                "geminifile refresh: refreshes the specified services\n" +
                "   [-n networking service] [-p pinger service] [-f binder services]\n" +
                "");
    }

    public static Map<String, String> argumentProcessor(String[] args) {

        // Maps the arguments with their respective arguments.
        Map<String, String> argumentMap = new LinkedHashMap<>();

        Pattern regSing = Pattern.compile("^(-)[a-zA-Z]{1,100}$"); // Regex for single hyphens
        Pattern regDoub = Pattern.compile("^(--)[a-zA-Z]{1,100}$"); // Regex for double hyphens

        // THE PROGRAM ALWAYS IGNORES THE FIRST ARGUMENT

        // This for loop is for getting all the arguments from the program.
        // Starts from 1 because the first argument has already been picked up
        for (int i = 1; i < args.length; i++) {

            // Checks whether there is an argument after
            String argAfter = "";
            try {
                if (!args[i + 1].startsWith("-")) {
                    argAfter = args[i + 1]; // Assigns into variable
                }
            } catch (IndexOutOfBoundsException ignored) { } // Just to catch out of bounds

            if (regSing.matcher(args[i]).matches()) {
                // If it follows the single hyphen regex
                String newArg = args[i].substring(1);
                for (Character ch: newArg.toCharArray()) {
                    argumentMap.put(ch.toString(), argAfter);
                }

            } else if (regDoub.matcher(args[i]).matches()) {
                // If it follows the double hyphen regex
                String newArg = args[i].substring(2); // Grabs only the word
                argumentMap.put(newArg, argAfter);
            }
        }

        return argumentMap;
    }

    public static void showVersion() {
        System.out.println("" +
                "###                ###\n" +
                "   ################   \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "       #      #       \n" +
                "   ################   \n" +
                "###                ###\n" +
                "GeminiFile v0.0.1 by Jason Christian\n" +
                "" +
                "");
    }

}
