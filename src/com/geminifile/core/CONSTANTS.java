package com.geminifile.core;

public class CONSTANTS {

    public static final int LOCALPORT = 43743;
    public static final int COMMPORT = 54638;
    public static final boolean VERBOSE = true;
    public static final int IPPINGERTHREADS = 16; // Number of threads used to ping all the ip range (more means faster response
    public static final int PINGTIMEOUT = 200; // in ms (Theoretical time to ping all ips (255 / IPPINGERTHREADS * PINGTIMEOUT))

}
