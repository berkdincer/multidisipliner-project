package com.audiometer.audiometerproject.algorithm;

/**
 * Represents the possible states of an audiometric test algorithm.
 * Used to track and communicate the progress of threshold determination.
 */
public enum TestState {
    /** Test has not started or is between frequencies */
    IDLE("Idle"),

    /** A tone is being presented to the patient */
    PRESENTING_TONE("Presenting Tone"),

    /** Waiting for the patient to respond (or timeout) */
    WAITING_RESPONSE("Waiting for Response"),

    /** Intensity is being increased (patient did not hear) */
    ASCENDING("Ascending (5 dB up)"),

    /** Intensity is being decreased (patient heard the tone) */
    DESCENDING("Descending (10 dB down)"),

    /** Threshold has been determined for the current frequency */
    THRESHOLD_FOUND("Threshold Found"),

    /** All frequencies have been tested for the current ear */
    COMPLETED("Test Completed");

    private final String description;

    TestState(String description) {
        this.description = description;
    }

    /**
     * Returns a human-readable description of this state.
     * @return state description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
