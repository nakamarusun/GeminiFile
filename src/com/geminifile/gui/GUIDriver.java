package com.geminifile.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GUIDriver extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml")); // Sets the main stage
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("img\\icon\\logo64.png"))); // Sets the application logo
        primaryStage.setTitle("GeminiFile Prototype 1"); // Title

        // Minimum sizes
        primaryStage.setMinWidth(600.0);
        primaryStage.setMinHeight(437.0);

        primaryStage.setScene(new Scene(root, 800, 600)); // Initial app size

        primaryStage.show(); // Show the app
    }

    public static void main(String[] args) {
        System.out.println("GeminiFile GUI v0.0.1");
        launch(args);
    }
}