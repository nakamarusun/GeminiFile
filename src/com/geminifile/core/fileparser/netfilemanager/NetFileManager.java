package com.geminifile.core.fileparser.netfilemanager;

import com.geminifile.core.service.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import static com.geminifile.core.CONSTANTS.FILEPORT;

public class NetFileManager {

    private static ServerSocket ssock;
    private static boolean stopSock;

    public static void start() {
        // Starts the service.
        // Open a new port at FilePort
        try {
            ssock = new ServerSocket(FILEPORT, 50, Service.getCurrentIp());

            while (true) {
                Socket sock = ssock.accept();

                Thread newConnection = new Thread(new NetFileReceiverThread(sock), "NetFileReceiver");
                newConnection.start();
                // Starts a new thread to process the message
            }
        } catch (SocketException e) {
            if (stopSock) {
                // Stops the service
            } else {
                // Restart PeerClientManager.
            }
        } catch (IOException e) {
            System.out.println("[NetFile] Socket error");
            e.printStackTrace();
        }
    }

    public static void stopService() {
        try {
            stopSock = true; // Signifies the interruption is because this method is invoked.
            ssock.close(); // Close ServerSocket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
