package com.geminifile.core;

import java.io.File;

public class CONSTANTS {

    public static final int LOCALPORT = 43743; // localhost port communication
    public static final int COMMPORT = 54638; // local network port communication
    public static final int FILEPORT = 54639;
    public static final boolean VERBOSE = true; // For verbose log in the cli

    public static final int IPPINGERTHREADS = 24; // Number of threads used to ping all the ip range (more means faster response.
    public static final int PINGTIMEOUT = 800; // in ms (Theoretical time to ping all ips (255 / IPPINGERTHREADS * PINGTIMEOUT))
    public static final int PORTCONNECTTIMEOUT = 3000; // in ms, max time to try to connect to a port.
    public static final int PINGEVERYXSECOND = 30; // in s, time to refresh all peers.

    public static final String MYBINDERSPATH = "." + File.separator; // Path to the binder's json path. Default is the same as GeminiFile executable
    public static final String MYBINDERSFILENAME = "MyBinders.json"; // Name of the MyBinders file

    public static final String TEMPNETFILEPATH = "." + File.separator;; // Path to the temporary folder to keep downloaded files.
    public static final String TEMPNETFOLDERNAME = "temp"; // Folder name

    public static final int BYTESIZE = 4096; // Divides the file into x amount of bytes to send.

    public static final int WAITUPDATEFOR = 10; // in s, the amount of time to wait for another update in a binder before sending a query message.

    public static final String LOGFILE = "gemini.log"; // log file name.
    public static final String LOGFILEPATH = "." + File.separator; // Path to the log file.

    public static final String CONFIGFILE = "gemini.conf"; // config file name.
    public static final String CONFIGFILEPATH = "." + File.separator; // Path to the config file.

}
