package com.geminifile.core;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static com.geminifile.core.CONSTANTS.LOGFILE;
import static com.geminifile.core.CONSTANTS.LOGFILEPATH;

public class GeminiLogger {

    private static FileHandler logFile;
    private static DefaultFileFormatter formatter;

    private static ConsoleHandler consoleHandler;

    public static void initialize() throws IOException {
        // Initializes the logger
        Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // Removes all of the default handlers.
//        for(Handler e : LOGGER.getParent().getHandlers()) {
//            LOGGER.getParent().removeHandler(e);
//        }
        LOGGER.setUseParentHandlers(false); // Unset the default handler

        // Makes a new handler and sets it
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new DefaultFormatter());
        LOGGER.addHandler(consoleHandler);

        // File handler
        logFile = new FileHandler(LOGFILEPATH + LOGFILE);
        DefaultFileFormatter formatter = new DefaultFileFormatter();
        logFile.setFormatter(formatter);
        LOGGER.addHandler(logFile);
    }

    public static ConsoleHandler getConsoleHandler() {
        return consoleHandler;
    }
}