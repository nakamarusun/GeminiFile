package com.geminifile.gui.canvas.bindercells;

import com.geminifile.core.fileparser.binder.Binder;
import javafx.scene.control.ListCell;

public class ListViewBinderCell extends ListCell<Binder> {

    @Override
    public void updateItem(Binder binder, boolean empty) {
        super.updateItem(binder, empty);
        if(binder != null) {
            BinderCell binderCell = new BinderCell();
//            binderCell.setValues(binder); // Sets all of the values.
            binderCell.setValues(binder);
            // Sets the graphic.
            setGraphic(binderCell.getCell());
        }
    }

}
