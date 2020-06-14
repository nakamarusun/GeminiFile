package com.geminifile.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller {

    // All of the FXML objects
    public AnchorPane mainCanvas; // Main canvas in the app.

    public ToggleButton aboutToggleNavigation;
    public ToggleButton settingsToggleNavigation;
    public ToggleButton bindersToggleNavigation;
    public ToggleButton homeToggleNavigation;

    // All of the pane objects
    public AnchorPane homePane;
    public AnchorPane bindersPane;

    public AnchorPane currentPane;

    private final ToggleGroup navigationToggle = new ToggleGroup();
    public VBox navigationBar;

    public void initialize() throws IOException {
        // Sets all of the navigation toggle buttons into a group
        aboutToggleNavigation.setToggleGroup(navigationToggle);
        settingsToggleNavigation.setToggleGroup(navigationToggle);
        bindersToggleNavigation.setToggleGroup(navigationToggle);
        homeToggleNavigation.setToggleGroup(navigationToggle);

        // Set the default value, home.
        homeToggleNavigation.setSelected(true);

        // Loads all of the panes
        homePane = FXMLLoader.load(getClass().getResource("home.fxml"));
        bindersPane = FXMLLoader.load(getClass().getResource("binders.fxml"));

        // Loads the homePane to the canvas
        currentPane = homePane;
        mainCanvas.getChildren().add(homePane);
        updateMainCanvasConstraints();
    }

    public void changePaneToHome() {
        changeMainCanvasPane(homePane);
        updateMainCanvasConstraints();
    }

    public void changePaneToBinders() {
        changeMainCanvasPane(bindersPane);
        updateMainCanvasConstraints();
    }

    private void changeMainCanvasPane(AnchorPane pane) {
        // Changes the main canvas panel into something else specified.
        mainCanvas.getChildren().remove(currentPane);
//        pane.setPrefWidth(mainCanvas.getWidth());
//        pane.setPrefHeight(mainCanvas.getHeight());
        currentPane = pane;
        mainCanvas.getChildren().add(currentPane);
    }

    public void updateMainCanvasConstraints() {
        AnchorPane.setRightAnchor(currentPane, 0.0);
        AnchorPane.setLeftAnchor(currentPane, 0.0);
        AnchorPane.setTopAnchor(currentPane, 0.0);
        AnchorPane.setBottomAnchor(currentPane, 0.0);
    }

}