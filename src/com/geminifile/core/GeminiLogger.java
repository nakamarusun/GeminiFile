package com.geminifile.core;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static com.geminifile.core.CONSTANTS.LOGFILE;
import static com.geminifile.core.CONSTANTS.LOGFILEPATH;

public class GeminiLogger {

    private static FileHandler logFile;

    private static boolean initialized = false;

    private static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void initialize(boolean showConsole, boolean writeToFile) throws IOException {
        if (!initialized) {

            // Removes all of the default handlers.
//        for(Handler e : LOGGER.getParent().getHandlers()) {
//            LOGGER.getParent().removeHandler(e);
//        }
            LOGGER.setUseParentHandlers(false); // Unset the default handler

            // Makes a new handler and sets it
            if (showConsole) {
                logToConsole();
            }
            // File handler
            if (writeToFile) {
                logToFile();
            }

            initialized = true;
        }
    }

    public static void logToFile() throws IOException {
        logFile = new FileHandler(LOGFILEPATH + LOGFILE);
        DefaultFileFormatter formatter = new DefaultFileFormatter();
        logFile.setFormatter(formatter);
        LOGGER.addHandler(logFile);
    }

    public static void logToConsole() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new DefaultFormatter());
        LOGGER.addHandler(consoleHandler);
    }

}