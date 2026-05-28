package com.audiometer.audiometerproject.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application class for the Audiometer Test System.
 * Loads the FXML layout and applies the CSS stylesheet.
 */
public class AudiometerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                AudiometerApp.class.getResource("/com/audiometer/audiometerproject/audiometer-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());

        // Load CSS
        String css = AudiometerApp.class.getResource(
                "/com/audiometer/audiometerproject/styles.css"
        ).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Audiometer Desktop UI");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
