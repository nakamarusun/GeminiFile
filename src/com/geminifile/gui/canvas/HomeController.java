package com.geminifile.gui.canvas;

import javafx.scene.text.Text;

public class HomeController {

    public Text binderCount;
    public Text lastSyncedDate;
    public Text selfIpAddress;

    public void initialize() {
        binderCount.setText("Jojof");
        selfIpAddress.setText("192.168.1.1");
    }
}