package com.geminifile.core;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DefaultFileFormatter extends Formatter {
    // Formatter for logging services.

    @Override
    public String format(LogRecord rec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
        Date time = new Date(rec.getMillis());

        // Puts all of the exceptions into the file.
        StringBuilder exceptions = new StringBuilder();
        if (rec.getThrown() != null) {
            for (StackTraceElement e : rec.getThrown().getStackTrace()) {
                exceptions.append(e.toString()).append("\n");
            }
        }

        return "[" + dateFormat.format(time) + "] " + rec.getLevel() + " - " + rec.getMessage() + "\n" + exceptions.toString();
    }

    @Override
    public String getHead(Handler h) {
        return "[GeminiFile log v0.0.1 at " + (new Date()).toString() + " ]\n\n";
    }

    @Override
    public String getTail(Handler h) {
        return "\n[End of GeminiFile log v0.0.1 at " + (new Date()).toString() + " ]\n";
    }
}
