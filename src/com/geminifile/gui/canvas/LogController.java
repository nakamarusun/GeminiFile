package com.geminifile.gui.canvas;

import com.geminifile.core.DefaultFormatter;
import com.geminifile.core.GeminiLogger;
import com.geminifile.core.service.Service;
import com.geminifile.gui.Controller;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.StreamHandler;

public class LogController {

    public Button logButton;
    public TextArea logTextArea;

    private ByteArrayOutputStream loggerContent;
    private PrintStream printStream;
    private StreamHandler logStream;

    public void initialize() throws IOException {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setLogController(this);

        // Initializes the geminifile logger
        GeminiLogger.initialize(false, false);

        loggerContent = new ByteArrayOutputStream();
        printStream = new PrintStream(loggerContent);
        logStream = new StreamHandler(printStream, new DefaultFormatter());

        Service.LOGGER.addHandler(logStream);

        // Initializes a scheduler to refreshes the log every time
        ScheduledExecutorService autoLogger = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread trd = new Thread(r, "autoLogger");
            trd.setDaemon(true); // Sets it into daemon, so it automatically quits when the program is closed.
            return trd;
        });
        autoLogger.scheduleAtFixedRate(this::refreshLogger, 500, 500, TimeUnit.MILLISECONDS); // Runs this function every 500 milliseconds
    }

    public void logButtonAction() {
        Service.LOGGER.info((new Date()).toString());
    }

    public void refreshLogger() {
        logStream.flush(); // Flushes the log
        logTextArea.appendText(loggerContent.toString());
        System.out.println(loggerContent.size());
        loggerContent.reset();
    }

}