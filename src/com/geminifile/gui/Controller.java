package com.geminifile.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller {

    // All of the FXML objects
    public AnchorPane mainCanvas; // Main canvas in the app.

    public ToggleButton logToggleNavigation;
    public ToggleButton settingsToggleNavigation;
    public ToggleButton bindersToggleNavigation;
    public ToggleButton homeToggleNavigation;

    // All of the pane objects
    public AnchorPane homePane;
    public AnchorPane bindersPane;
    public AnchorPane settingsPane;
    public AnchorPane logPane;

    public AnchorPane currentPane; // Reference to the current pane

    private final ToggleGroup navigationToggle = new ToggleGroup(); // Navigation button toggle controller
    public VBox navigationBar; // VBox for navigationBar

    public void initialize() throws IOException {
        // Sets all of the navigation toggle buttons into a group
        logToggleNavigation.setToggleGroup(navigationToggle);
        settingsToggleNavigation.setToggleGroup(navigationToggle);
        bindersToggleNavigation.setToggleGroup(navigationToggle);
        homeToggleNavigation.setToggleGroup(navigationToggle);

        // Set the default value, home.
        homeToggleNavigation.setSelected(true);

        // Loads all of the panes
        homePane = FXMLLoader.load(getClass().getResource("home.fxml"));
        bindersPane = FXMLLoader.load(getClass().getResource("binders.fxml"));
        settingsPane = FXMLLoader.load(getClass().getResource("settings.fxml"));
        logPane = FXMLLoader.load(getClass().getResource("log.fxml"));

        // Loads the homePane to the canvas
        currentPane = homePane;
        mainCanvas.getChildren().add(homePane);
        updateMainCanvasConstraints();
    }

    public void changePaneToHome() {
        reToggleNavigationButton(homeToggleNavigation);
        changeMainCanvasPane(homePane);
        updateMainCanvasConstraints();
    }

    public void changePaneToBinders() {
        reToggleNavigationButton(bindersToggleNavigation);
        changeMainCanvasPane(bindersPane);
        updateMainCanvasConstraints();
    }

    public void changePaneToSettings() {
        reToggleNavigationButton(settingsToggleNavigation);
        changeMainCanvasPane(settingsPane);
        updateMainCanvasConstraints();
    }

    public void changePaneToLog() {
        reToggleNavigationButton(logToggleNavigation);
        changeMainCanvasPane(logPane);
        updateMainCanvasConstraints();
    }

    private void changeMainCanvasPane(AnchorPane pane) {
        // Changes the main canvas panel into something else specified.
        if (currentPane != pane) {
            mainCanvas.getChildren().remove(currentPane);
//        pane.setPrefWidth(mainCanvas.getWidth());
//        pane.setPrefHeight(mainCanvas.getHeight());
            currentPane = pane;
            mainCanvas.getChildren().add(currentPane);
        }
    }

    private void reToggleNavigationButton(Toggle toggleButton) {
        // If a current button has been deactivated, that means that no button is active. So, reactivate it.
        if (navigationToggle.getSelectedToggle() == null) {
            navigationToggle.selectToggle(toggleButton);
        }
    }

    public void updateMainCanvasConstraints() {
        AnchorPane.setRightAnchor(currentPane, 0.0);
        AnchorPane.setLeftAnchor(currentPane, 0.0);
        AnchorPane.setTopAnchor(currentPane, 0.0);
        AnchorPane.setBottomAnchor(currentPane, 0.0);
    }

}