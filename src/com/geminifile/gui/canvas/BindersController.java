package com.geminifile.gui.canvas;

import com.geminifile.core.fileparser.binder.Binder;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.gui.Controller;
import com.geminifile.gui.Refresh;
import com.geminifile.gui.canvas.bindercells.ListViewBinderCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class BindersController implements Refresh {

    private final ObservableList<Binder> observableList = FXCollections.observableArrayList();

    public ListView<Binder> binderListView; // Binder list view

    public void initialize() {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setBindersController(this);
        setListView();
    }

    public void onRefresh() {
        if (BinderManager.getAllBinders().size() != observableList.size()) {
            observableList.setAll(BinderManager.getAllBinders()); // Gets all of the items from all of the binders.
        }
    }

    public void setListView() {
        binderListView.setItems(observableList); // Sets all of the item to the binderListView
        binderListView.setCellFactory(new Callback<ListView<Binder>, ListCell<Binder>>() { // Sets the cell factory to created based on the ListViewBinderCell
            @Override
            public ListCell<Binder> call(ListView<Binder> binderListView) {
                return new ListViewBinderCell();
            }
        });
    }
}
