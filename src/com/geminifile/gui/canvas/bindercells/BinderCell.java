package com.geminifile.gui.canvas.bindercells;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.service.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

public class BinderCell {

    public Text binderName;
    public Text binderId;
    public Text binderPath;
    public Text binderLastModified;

    private AnchorPane cell; // Sets the main stage

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
        // Sets all of the values from the binder.
        binderName.setText(binder.getName());
        binderId.setText(binder.getId());
        binderPath.setText(binder.getDirectory().getAbsolutePath());
        binderLastModified.setText((new Date(binder.getDirectoryLastModified()).toString()));
    }

    AnchorPane getCell() {
        return cell;
    }

}