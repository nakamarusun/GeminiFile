package com.geminifile.gui.canvas;

import com.geminifile.gui.Controller;
import com.geminifile.gui.Refresh;

public class BindersController implements Refresh {

    public void initialize() {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setBindersController(this);
    }

    public void onRefresh() {

    }
}
