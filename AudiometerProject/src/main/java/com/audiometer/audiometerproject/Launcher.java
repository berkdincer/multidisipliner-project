package com.audiometer.audiometerproject;

import com.audiometer.audiometerproject.ui.AudiometerApp;
import javafx.application.Application;

/**
 * Main entry point for the Audiometer Test System.
 * This class exists because JavaFX requires the main class
 * to NOT extend Application when running from a modular JAR.
 *
 * <p>Delegates to {@link AudiometerApp} which is the actual
 * JavaFX Application subclass.</p>
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(AudiometerApp.class, args);
    }
}
