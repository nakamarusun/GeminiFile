package com.geminifile.core.service;

import static com.geminifile.core.CONSTANTS.*;

public class PingerThread implements Runnable {

    // Determines the ip number they will be pinging
    private final int number;

    public PingerThread(int number) {
        this.number = number;
    }

    @Override
    public void run() {
        // TODO: log all the found ip addresses

    }
}
