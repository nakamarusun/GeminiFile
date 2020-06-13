package com.geminifile.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

public class Controller {

    public AnchorPane mainCanvas;
    public ToggleButton aboutToggleNavigation;
    public ToggleButton settingsToggleNavigation;
    public ToggleButton bindersToggleNavigation;
    public ToggleButton homeToggleNavigation;

    private final ToggleGroup navigationToggle = new ToggleGroup();

    public void initialize() {
        // Sets all of the navigation toggle buttons into a group
        aboutToggleNavigation.setToggleGroup(navigationToggle);
        settingsToggleNavigation.setToggleGroup(navigationToggle);
        bindersToggleNavigation.setToggleGroup(navigationToggle);
        homeToggleNavigation.setToggleGroup(navigationToggle);

        // Set the default value, home.
        homeToggleNavigation.setSelected(true);
    }


}