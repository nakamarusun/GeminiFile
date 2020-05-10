package com.geminifile.core;

public class CONSTANTS {

    public static final int LOCALPORT = 43743; // localhost port communication
    public static final int COMMPORT = 54638; // local network port communication
    public static final boolean VERBOSE = true; // For verbose log in the cli

    public static final int IPPINGERTHREADS = 16; // Number of threads used to ping all the ip range (more means faster response.
    public static final int PINGTIMEOUT = 200; // in ms (Theoretical time to ping all ips (255 / IPPINGERTHREADS * PINGTIMEOUT))
    public static final int PORTCONNECTTIMEOUT = 2000; // in ms, max time to try to connect to a port.

}
