package com.geminifile.gui.canvas;

import com.geminifile.core.CONSTANTS;
import com.geminifile.core.GeminiLogger;
import com.geminifile.core.service.Service;
import com.geminifile.gui.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

public class SettingsController {

    public CheckBox saveLogsCheck;
    public CheckBox autoStartCheck;
    public Button saveSettingsButton;

    JSONObject json = new JSONObject();

    public void initialize() throws IOException {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setSettingsController(this);

        // Reads from the available config file, if does not exist, then create it.
        File confFile = new File(CONSTANTS.CONFIGFILEPATH + CONSTANTS.CONFIGFILE);

        if (!confFile.exists()) {
            try {
                confFile.createNewFile();
                writeDefaultFile();
            } catch (IOException e) {
                Service.LOGGER.severe("Error creating logfile");
                Service.LOGGER.log(Level.SEVERE, "Exception", e);
            }
        }
        loadConfigFile();
    }

    public void writeDefaultFile() throws IOException {
        // Sets all of the default values
        json.put("autostart", false);
        json.put("savelogs", true);
        saveConfigFile();
    }

    public void saveConfigFileButton() throws IOException {
        json.put("autostart", autoStartCheck.isSelected());
        json.put("savelogs", saveLogsCheck.isSelected());
        saveConfigFile();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Settings saved !", ButtonType.OK);
        alert.show();
    }

    public void saveConfigFile() throws IOException {
        FileWriter fileWriter = new FileWriter(CONSTANTS.CONFIGFILEPATH + CONSTANTS.CONFIGFILE); // Opens a new instance of the fileWriter

        // Writes into the file
        json.write(fileWriter);
        fileWriter.flush(); // Don't forget to flush and close it
        fileWriter.close();
    }

    public void loadConfigFile() throws IOException {
        Scanner fileReader = new Scanner(new File(CONSTANTS.CONFIGFILEPATH + CONSTANTS.CONFIGFILE));
        StringBuilder jsonString = new StringBuilder();

        // Reads the file and puts it into the string builder.
        while (fileReader.hasNext()) {
            jsonString.append(fileReader.nextLine());
        }
        // Puts it into json.

        json = new JSONObject(jsonString.toString());

        if (json.optBoolean("autostart")) {
            autoStartCheck.setSelected(true);
            // Starts the geminifile service
        }

        if (json.optBoolean("savelogs")) {
            saveLogsCheck.setSelected(true);
            GeminiLogger.logToFile();
        }
    }
}