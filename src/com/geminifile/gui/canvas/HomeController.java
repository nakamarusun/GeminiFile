package com.geminifile.gui.canvas;

import com.geminifile.core.MathUtil;
import com.geminifile.core.fileparser.binder.BinderManager;
import com.geminifile.core.service.Service;
import com.geminifile.gui.Controller;
import com.geminifile.gui.Refresh;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;

public class HomeController implements Refresh {

    // Texts
    public Text binderCount;
    public Text selfIpAddress;

    public ImageView logoMainFlap;
    public ImageView logoSubFlap;

    public RotateTransition mainFlapRotator;
    public RotateTransition subFlapRotator;

    public Button logoButton;

    public Circle logoCircle;
    public final Paint defaultColor = Paint.valueOf("#1c2454");
    public final Paint hoverColor = Paint.valueOf("#243d75");
    public final Paint clickColor = Paint.valueOf("#3b689c");

    public Text logoMainText;
    public Text logoSubText;

    private boolean serviceStarted = false;
    Thread geminiService = new Thread("GeminiFileService");

    public void initialize() throws IOException {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setHomeController(this);

        binderCount.setText("-");
        selfIpAddress.setText("-");

        // Sets the animation for the main flap
        mainFlapRotator = new RotateTransition();
        mainFlapRotator.setAutoReverse(true);
        mainFlapRotator.setNode(logoMainFlap);
        mainFlapRotator.setInterpolator(Interpolator.EASE_BOTH);
        mainFlapRotator.setOnFinished(actionEvent -> regenerateMainFlapRotator());

        // Sets the animation for sub flap
        subFlapRotator = new RotateTransition();
        subFlapRotator.setAutoReverse(true);
        subFlapRotator.setNode(logoSubFlap);
        subFlapRotator.setInterpolator(Interpolator.EASE_BOTH);
        subFlapRotator.setOnFinished(actionEvent -> regenerateSubFlapRotator());

        logoCircle.setFill(defaultColor);
    }

    public void onRefresh() {
        if (serviceStarted) {

            binderCount.setText(String.valueOf(BinderManager.getAllBinders().size()));
            if (Service.getCurrentIp() != null) {
                selfIpAddress.setText(Service.getCurrentIp().getHostAddress());
            }

        }
    }

    public void regenerateMainFlapRotator() {
        mainFlapRotator.stop();
        mainFlapRotator.setDuration(Duration.millis(MathUtil.randomRange(5000, 12000)));
        mainFlapRotator.setByAngle(MathUtil.randomRange(180, 500));
        mainFlapRotator.setCycleCount(MathUtil.randomRange(1, 3));
        mainFlapRotator.play();
    }

    public void regenerateSubFlapRotator() {
        subFlapRotator.stop();
        subFlapRotator.setDuration(Duration.millis(MathUtil.randomRange(3000, 7000)));
        subFlapRotator.setByAngle(MathUtil.randomRange(360, 900));
        subFlapRotator.setCycleCount(MathUtil.randomRange(1, 3));
        subFlapRotator.play();
    }

    public void hoverCircle() {
        logoCircle.setFill(hoverColor);
    }

    public void clickCircle() {
        logoCircle.setFill(clickColor);
        // If service hasn't been started yet, start service.
        if (!serviceStarted) {
            serviceStarted = true;
            // Change texts
            logoMainText.setText("Started");
            logoSubText.setText("STATUS OK!");

            // Start service.
            startGeminiFileService();

            // Start animations
            regenerateMainFlapRotator();
            regenerateSubFlapRotator();
        } else {
            serviceStarted = false;
            // Change texts
            logoMainText.setText("Start");
            logoSubText.setText("GeminiFile");

            // Stops service
            stopGeminiFileService();

            // Reset all of the text.
            binderCount.setText("-");
            selfIpAddress.setText("-");

            // Stop animations
            mainFlapRotator.stop();
            subFlapRotator.stop();
            Controller.getMainControllerReference().getBindersController().refreshBindersCellList();
        }
    }

    public void startGeminiFileService() {
        geminiService = new Thread(Service::start, "GeminiFileService");
        geminiService.setDaemon(true);
        geminiService.start();
    }

    public void stopGeminiFileService() {
        Service.stopService();
    }

    public void exitCircle() {
        logoCircle.setFill(defaultColor);
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }
}