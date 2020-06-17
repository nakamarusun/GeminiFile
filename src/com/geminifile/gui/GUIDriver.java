package com.geminifile.gui;

import com.geminifile.core.CLIDriver;
import com.geminifile.core.service.Service;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class GUIDriver extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml")); // Sets the main stage
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/geminifile/gui/img/icon/logo64.png"))); // Sets the application logo
        primaryStage.setTitle("GeminiFile Prototype 1"); // Title

        // Minimum sizes
        primaryStage.setMinWidth(600.0);
        primaryStage.setMinHeight(437.0);

        primaryStage.setScene(new Scene(root, 800, 600)); // Initial app size

        primaryStage.setOnCloseRequest(windowEvent -> {
            Service.stopService();
        }); // Stops the service when app is closed.

        primaryStage.show(); // Show the app
    }

    public static void main(String[] args) {
        System.out.println("GeminiFile GUI v0.0.1");

        // If there is any argument in starting the program then, start the CLI version instead.
        if (args.length > 0) {
            CLIDriver.main(args);
        } else {
            launch(args);
        }
    }
}