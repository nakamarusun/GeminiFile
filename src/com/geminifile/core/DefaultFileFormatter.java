package com.geminifile.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DefaultFileFormatter extends Formatter {
    // Formatter for logging services.

    @Override
    public String format(LogRecord rec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date time = new Date(rec.getMillis());
        return "[" + dateFormat.format(time) + "] " + rec.getLevel() + " - " + rec.getMessage() + "\n";
    }

    @Override
    public String getHead(Handler h) {
        return "[GeminiFile log v0.0.1]\n\n";
    }

    @Override
    public String getTail(Handler h) {
        return "\n[End GeminiFile log v0.0.1]";
    }
}
