package com.geminifile.gui.canvas;

import com.geminifile.gui.Controller;

public class BindersController {

    public void initialize() {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setBindersController(this);
    }
}
