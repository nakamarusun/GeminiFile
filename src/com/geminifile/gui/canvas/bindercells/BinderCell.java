package com.geminifile.gui.canvas.bindercells;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.Service;
import com.geminifile.gui.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;

public class BinderCell {

    public Text binderName;
    public Text binderId;
    public Text binderPath;
    public Text binderLastModified;

    private AnchorPane cell; // Sets the main stage

    private Binder binder;

    public BinderCell() {
        // Loads the cell
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bindercell.fxml"));
            loader.setController(this);
            cell = loader.load();
        } catch (IOException e) {
            Service.LOGGER.log(Level.SEVERE, "Exception", e);
        }
    }

    public void setValues(Binder binder) {
        // Sets the binder
        this.binder = binder;
        // Sets all of the values from the binder.
        binderName.setText(binder.getName());
        binderId.setText(binder.getId());
        binderPath.setText(binder.getDirectory().getAbsolutePath());
        binderLastModified.setText((new Date(binder.getDirectoryLastModified()).toString()));
    }

    AnchorPane getCell() {
        return cell;
    }

    public void deleteButton() {
        // Shows the prompt to delete the binder
        // Adds to the binder
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm ?");
        alert.setHeaderText("Delete binder ?");
        alert.setContentText("You can add it again later.");

        // Shows the confirmation
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // If user clicks ok
            // Adds the new binder to the binder list.
            BinderManager.removeBinder(binder);
            Controller.getMainControllerReference().getBindersController().refreshBindersCellList();
        }
        alert.close();
    }

}