package com.audiometer.audiometerproject.model;

import java.time.LocalDateTime;

/**
 * Represents a single tone presentation and the patient's response.
 * This is an immutable data class — once created, its state cannot change.
 *
 * Each TestResult records:
 * - Which frequency was tested
 * - At what intensity (dBHL)
 * - Which ear
 * - Whether the patient responded (heard the tone)
 * - When the test occurred
 */
public final class TestResult {
    private final Frequency frequency;
    private final int intensityDb;
    private final Ear ear;
    private final boolean responded;
    private final LocalDateTime timestamp;

    /**
     * Creates a new immutable TestResult.
     *
     * @param frequency  the test frequency
     * @param intensityDb the intensity level in dBHL
     * @param ear        which ear was tested
     * @param responded  true if the patient indicated hearing the tone
     */
    public TestResult(Frequency frequency, int intensityDb, Ear ear, boolean responded) {
        this.frequency = frequency;
        this.intensityDb = intensityDb;
        this.ear = ear;
        this.responded = responded;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a new immutable TestResult with a specific timestamp.
     *
     * @param frequency   the test frequency
     * @param intensityDb the intensity level in dBHL
     * @param ear         which ear was tested
     * @param responded   true if the patient indicated hearing the tone
     * @param timestamp   when the test was conducted
     */
    public TestResult(Frequency frequency, int intensityDb, Ear ear, boolean responded, LocalDateTime timestamp) {
        this.frequency = frequency;
        this.intensityDb = intensityDb;
        this.ear = ear;
        this.responded = responded;
        this.timestamp = timestamp;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public int getIntensityDb() {
        return intensityDb;
    }

    public Ear getEar() {
        return ear;
    }

    public boolean isResponded() {
        return responded;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("TestResult{freq=%s, dB=%d, ear=%s, responded=%s, time=%s}",
                frequency.getDisplayString(), intensityDb, ear.getDisplayName(),
                responded, timestamp);
    }
}
