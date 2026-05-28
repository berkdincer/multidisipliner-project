module com.audiometer.audiometerproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;

    // Open packages to JavaFX for FXML reflection
    opens com.audiometer.audiometerproject.ui to javafx.fxml;

    // Export all packages
    exports com.audiometer.audiometerproject;
    exports com.audiometer.audiometerproject.model;
    exports com.audiometer.audiometerproject.algorithm;
    exports com.audiometer.audiometerproject.serial;
    exports com.audiometer.audiometerproject.ui;
}