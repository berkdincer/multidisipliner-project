package com.audiometer.audiometerproject.ui;

import com.audiometer.audiometerproject.algorithm.*;
import com.audiometer.audiometerproject.model.*;
import com.audiometer.audiometerproject.serial.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main controller for the Audiometer Test System.
 * Coordinates between the UI, serial communication, algorithm, and audiogram rendering.
 */
public class MainController implements Initializable {

    // ========== FXML Components ==========

    @FXML private Canvas audiogramCanvas;
    @FXML private ComboBox<Integer> frequencyCombo;
    @FXML private Spinner<Integer> dbSpinner;
    @FXML private Button rightEarButton;
    @FXML private Button leftEarButton;
    @FXML private Button heardButton;
    @FXML private Button playSoundButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    
    @FXML private Button startAutoTestButton;
    @FXML private Button noResponseButton;
    @FXML private Label algoStateLabel;

    // ========== Internal State ==========

    private AudiogramRenderer audiogramRenderer;
    private SerialCommunicator serialCommunicator;
    
    private ThresholdData rightEarData;
    private ThresholdData leftEarData;
    private Ear selectedEar = Ear.RIGHT;

    // OOP Integration classes
    private AudiogramSession currentSession;
    private TestAlgorithm testAlgorithm;

    // ========== Initialization ==========

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Init data models and OOP Session
        rightEarData = new ThresholdData(Ear.RIGHT);
        leftEarData = new ThresholdData(Ear.LEFT);
        
        // Setup Patient Info (using Builder Pattern as requested by OOP specs)
        PatientInfo patient = new PatientInfo.Builder()
                .firstName("John")
                .lastName("Doe")
                .age(35)
                .notes("Standard checkup")
                .build();
                
        currentSession = new AudiogramSession(patient);

        // Init UI
        setupFrequencyCombo();
        setupDbSpinner();
        setupAudiogram();
        setupSerial();

        setStatus("System ready.");
    }

    private void setupFrequencyCombo() {
        Integer[] frequencies = {250, 500, 1000, 2000, 4000, 8000};
        frequencyCombo.setItems(FXCollections.observableArrayList(frequencies));
        frequencyCombo.getSelectionModel().selectFirst();
    }

    private void setupDbSpinner() {
        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 120, 0, 5);
        dbSpinner.setValueFactory(factory);
    }

    private void setupAudiogram() {
        audiogramRenderer = new AudiogramRenderer(audiogramCanvas);
        redrawAudiogram();
    }

    private void setupSerial() {
        serialCommunicator = new JSerialCommAdapter();

        // Observer pattern: listen for RESPONSE from hardware
        serialCommunicator.setResponseListener(message -> {
            Platform.runLater(() -> handleHeard(null));
        });

        boolean connected = serialCommunicator.connect("COM2", 2400);
        if (connected) {
            setStatus("Connected to COM2.");
        } else {
            setStatus("COM2 not available — running offline.");
        }
    }

    // ========== Ear Selection ==========

    @FXML
    void handleRightEar(ActionEvent event) {
        selectedEar = Ear.RIGHT;
        rightEarButton.getStyleClass().setAll("right-ear-active");
        leftEarButton.getStyleClass().setAll("left-ear-inactive");
        setStatus("Selected: " + Ear.RIGHT.getDisplayName());
    }

    @FXML
    void handleLeftEar(ActionEvent event) {
        selectedEar = Ear.LEFT;
        leftEarButton.getStyleClass().setAll("left-ear-active");
        rightEarButton.getStyleClass().setAll("right-ear-inactive");
        setStatus("Selected: " + Ear.LEFT.getDisplayName());
    }

    // ========== Auto Test (Hughson-Westlake) ==========

    @FXML
    void handleStartAutoTest(ActionEvent event) {
        testAlgorithm = new HughsonWestlake();
        
        // Initial setup for algo
        int startDb = testAlgorithm.getStartingIntensity(); // usually 30 dB
        dbSpinner.getValueFactory().setValue(startDb);
        
        updateAlgoUI();
        setStatus("Auto Test started. Sending initial tone...");
        
        // Play the first tone
        handlePlaySound(null);
    }
    
    private void updateAlgoUI() {
        if (testAlgorithm == null) {
            algoStateLabel.setText("State: IDLE");
            startAutoTestButton.setDisable(false);
        } else {
            TestState state = testAlgorithm.getCurrentState();
            algoStateLabel.setText("State: " + state.name() + " @ " + testAlgorithm.getNextIntensity() + " dB");
            startAutoTestButton.setDisable(true); // Disable while running
        }
    }
    
    private void checkAlgorithmCompletion() {
        if (testAlgorithm != null) {
            if (testAlgorithm.isThresholdFound()) {
                int thresholdDb = testAlgorithm.getThresholdDb();
                int freq = frequencyCombo.getValue();
                
                // Record threshold
                saveThreshold(freq, thresholdDb);
                
                setStatus("Threshold FOUND: " + thresholdDb + " dBHL");
                testAlgorithm = null; // Reset algo
                updateAlgoUI();
            } else {
                // Algo not finished, update UI to show next step and play it
                dbSpinner.getValueFactory().setValue(testAlgorithm.getNextIntensity());
                updateAlgoUI();
                
                // Automatically play the next sound in the sequence
                handlePlaySound(null);
            }
        }
    }

    // ========== Test Actions ==========

    @FXML
    void handlePlaySound(ActionEvent event) {
        int freq = frequencyCombo.getValue();
        int db = dbSpinner.getValue();

        String command = SerialCommandBuilder.buildToneCommand(freq, db);

        if (serialCommunicator.isConnected()) {
            serialCommunicator.sendCommand(command);
        }

        setStatus("Playing: " + freq + " Hz @ " + db + " dBHL (" + selectedEar.getDisplayName() + ")");
    }

    @FXML
    void handleHeard(ActionEvent event) {
        int freq = frequencyCombo.getValue();
        int db = dbSpinner.getValue();
        Frequency frequency = Frequency.fromValue(freq);
        
        // Log to session (OOP Requirement)
        TestResult result = new TestResult(frequency, db, selectedEar, true);
        currentSession.addTestResult(result);
        
        if (testAlgorithm != null) {
            // Feed response to algorithm
            testAlgorithm.recordResponse(true);
            setStatus("Patient heard tone. Algorithm calculating next step...");
            checkAlgorithmCompletion();
        } else {
            // Manual mode: just record threshold immediately
            saveThreshold(freq, db);
            setStatus("Threshold recorded (Manual): " + freq + " Hz = " + db + " dBHL");
        }
    }
    
    @FXML
    void handleNoResponse(ActionEvent event) {
        int freq = frequencyCombo.getValue();
        int db = dbSpinner.getValue();
        Frequency frequency = Frequency.fromValue(freq);
        
        // Log to session (OOP Requirement)
        TestResult result = new TestResult(frequency, db, selectedEar, false);
        currentSession.addTestResult(result);
        
        if (testAlgorithm != null) {
            // Feed response to algorithm
            testAlgorithm.recordResponse(false);
            setStatus("No response. Algorithm calculating next step...");
            checkAlgorithmCompletion();
        } else {
            setStatus("No response at " + db + " dBHL.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        rightEarData.clear();
        leftEarData.clear();
        currentSession = new AudiogramSession(currentSession.getPatient()); // reset session
        testAlgorithm = null;
        updateAlgoUI();
        redrawAudiogram();
        setStatus("Audiogram cleared.");
    }

    // ========== Helpers ==========
    
    private void saveThreshold(int freq, int db) {
        Frequency frequency = Frequency.fromValue(freq);
        if (selectedEar == Ear.RIGHT) {
            rightEarData.addThreshold(frequency, db);
        } else {
            leftEarData.addThreshold(frequency, db);
        }
        redrawAudiogram();
    }

    private void redrawAudiogram() {
        audiogramRenderer.draw(rightEarData, leftEarData);
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
