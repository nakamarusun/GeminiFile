package com.geminifile.gui.canvas;

import com.geminifile.core.MathUtil;
import com.geminifile.core.service.Service;
import com.geminifile.gui.Controller;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class HomeController {

    // Texts
    public Text binderCount;
    public Text lastSyncedDate;
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

    public void initialize() {
        // Sets the reference for easy access.
        Controller.getMainControllerReference().setHomeController(this);

        binderCount.setText("-");
        selfIpAddress.setText("-");
        lastSyncedDate.setText("-");

        // Sets the animation for the main flap
        mainFlapRotator = new RotateTransition();
        mainFlapRotator.setOnFinished(actionEvent -> regenerateMainFlapRotator());

        // Sets the animation for sub flap
        subFlapRotator = new RotateTransition();
        subFlapRotator.setOnFinished(actionEvent -> regenerateSubFlapRotator());

        logoCircle.setFill(defaultColor);
    }

    public void regenerateMainFlapRotator() {
        mainFlapRotator.stop();
        mainFlapRotator.setInterpolator(Interpolator.EASE_BOTH);
        mainFlapRotator.setDuration(Duration.millis(MathUtil.randomRange(5000, 12000)));
        mainFlapRotator.setNode(logoMainFlap);
        mainFlapRotator.setByAngle(MathUtil.randomRange(180, 500));
        mainFlapRotator.setCycleCount(2);
        mainFlapRotator.setAutoReverse(MathUtil.randomBoolean());
        mainFlapRotator.play();
    }

    public void regenerateSubFlapRotator() {
        subFlapRotator.stop();
        mainFlapRotator.setInterpolator(Interpolator.EASE_BOTH);
        subFlapRotator.setDuration(Duration.millis(MathUtil.randomRange(3000, 7000)));
        subFlapRotator.setNode(logoSubFlap);
        subFlapRotator.setByAngle(MathUtil.randomRange(360, 900));
        subFlapRotator.setCycleCount(2);
        subFlapRotator.setAutoReverse(MathUtil.randomBoolean());
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

            // Stop animations
            mainFlapRotator.stop();
            subFlapRotator.stop();
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
}