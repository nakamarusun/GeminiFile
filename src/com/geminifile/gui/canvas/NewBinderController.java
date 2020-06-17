package com.geminifile.gui.canvas;

import com.geminifile.core.MathUtil;
import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.gui.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class NewBinderController {

    // Text fields for inputs.
    public TextField newNameField;
    public TextField newIdField;
    public TextField newPathField;

    private Stage stage;

    public void initialize() {
        stage = BindersController.getNewBinderStage();
    }

    public void saveButton() {
        if (checkVariables()) {

            // Checks if id field is empty, and generates a new id
            if (newIdField.getText().length() == 0) {
                newIdField.setText(MathUtil.generateRandomAlphaNum(7));
            }

            // Adds to the binder
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm ?");
            alert.setHeaderText("Add binder ?");
            alert.setContentText(
                    "Binder Name :  " + newNameField.getText() + "\n" +
                    "Binder ID   :  " + newIdField.getText() + "\n" +
                    "Binder Path :  " + newPathField.getText());

            // Shows the confirmation
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                // If user clicks ok
                // Adds the new binder to the binder list.
                Binder binder = new Binder(newNameField.getText(), newIdField.getText(), new File(newPathField.getText()));
                BinderManager.addNewBinder(binder);
                Controller.getMainControllerReference().getBindersController().refreshBindersCellList(); // Refreshes the observable list
                stage.close();
            }
            alert.close();
        }
    }

    public void cancelButton() {
        stage.close();
    }

    public void browsePathDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser(); // New directory chooser object
        directoryChooser.setInitialDirectory(new File("." + File.separator)); // Sets the initial directory
        File selectedDir = directoryChooser.showDialog(stage); // Shows the actual prompt

        newPathField.setText(selectedDir.getAbsolutePath()); // Sets the text field for path
        newPathField.positionCaret(newPathField.getText().length()); // Moves the caret to the rightmost position
    }

    private boolean checkVariables() {
        StringBuilder errors = new StringBuilder();
        // Checks for name
        if (newNameField.getText().length() <= 0) {
            errors.append("Insert a valid name !\n");
        }

        // If path doesn't meet the criteria
        File dir = new File(newPathField.getText());
        if (!(dir.exists() && dir.isDirectory()) || newPathField.getText().length() <= 0) {
            errors.append("Insert a valid path !\n");
        }

        // If the error string builder is not empty, then there is an error present.
        if (errors.length() != 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setHeaderText("Error creating binder"); // Sets the error header
            alert.setContentText(errors.toString()); // Sets the error message
            alert.show();
            return false;
        }

        return true;
    }

}
