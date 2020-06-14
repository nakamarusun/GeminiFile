package com.geminifile.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DefaultFormatter extends Formatter {
    // Formatter for logging services.

    @Override
    public String format(LogRecord rec) {
        return "{" + rec.getLevel() + "} - " + rec.getMessage() + "\n";
    }

    @Override
    public String getHead(Handler h) {
        return "{GeminiFile log v0.0.1}\n";
    }

    @Override
    public String getTail(Handler h) {
        return "\n{End GeminiFile log v0.0.1}";
    }
}
