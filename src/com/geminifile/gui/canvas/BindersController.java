package com.geminifile.gui.canvas;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.gui.Controller;
import com.geminifile.gui.Refresh;
import com.geminifile.gui.canvas.bindercells.ListViewBinderCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class BindersController implements Refresh {

    private final ObservableList<Binder> observableList = FXCollections.observableArrayList();
    private static Stage newBinderStage;

    public ListView<Binder> binderListView = null; // Binder list view
    public Button addBinderButton;
    public Button refreshButton;

    public void initialize() {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setBindersController(this);
        binderListView.setEditable(false); // Sets it to not be editable
        refreshBindersCellList();
    }

    public void onRefresh() {
        addBinderButton.setDisable(!Controller.getMainControllerReference().getHomeController().isServiceStarted());
        refreshButton.setDisable(!Controller.getMainControllerReference().getHomeController().isServiceStarted());
    }

    public void addButton() throws IOException {
        // Only show the modal when service is started.
        if (Controller.getMainControllerReference().getHomeController().isServiceStarted()) {
            // Shows the prompt to add a new binder
            newBinderStage = new Stage(); // Creates the new stage. and puts it into static variable reference.

            AnchorPane newBinderPane = FXMLLoader.load(getClass().getResource("newbinder.fxml"));
            newBinderStage.setResizable(false); // Not resizable
            newBinderStage.setScene(new Scene(newBinderPane)); // Sets the scene to the new set AnchorPane.
            newBinderStage.initModality(Modality.APPLICATION_MODAL); // Sets so that the main app cannot be interacted with when this window is active.
            newBinderStage.setTitle("New binder"); // Sets the title

            newBinderStage.show();
        }
    }

    public static Stage getNewBinderStage() {
        return newBinderStage;
    }

    public void refreshBindersCellList() {
        binderListView.getItems().clear();
        observableList.setAll(BinderManager.getAllBinders()); // Gets all of the items from all of the binders.
        binderListView.setItems(observableList); // Sets all of the item to the binderListView
        binderListView.setCellFactory(new Callback<ListView<Binder>, ListCell<Binder>>() { // Sets the cell factory to created based on the ListViewBinderCell
            @Override
            public ListCell<Binder> call(ListView<Binder> binderListView) {
                return new ListViewBinderCell();
            }
        });
    }
}
