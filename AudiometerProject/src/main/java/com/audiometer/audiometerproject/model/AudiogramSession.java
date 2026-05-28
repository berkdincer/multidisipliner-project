package com.audiometer.audiometerproject.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete audiometric test session.
 * Aggregates patient info, threshold data for both ears,
 * and a full log of all test presentations.
 *
 * This is the top-level model object for an audiometry exam.
 */
public class AudiogramSession {
    private final PatientInfo patient;
    private final ThresholdData rightEarData;
    private final ThresholdData leftEarData;
    private final List<TestResult> testLog;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * Creates a new session for the given patient.
     *
     * @param patient the patient being tested
     */
    public AudiogramSession(PatientInfo patient) {
        this.patient = patient;
        this.rightEarData = new ThresholdData(Ear.RIGHT);
        this.leftEarData = new ThresholdData(Ear.LEFT);
        this.testLog = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }

    /**
     * Records a test result and updates threshold data if a threshold was found.
     *
     * @param result the test result to add
     */
    public void addTestResult(TestResult result) {
        testLog.add(result);
    }

    /**
     * Records a threshold value for the specified ear and frequency.
     *
     * @param frequency the frequency tested
     * @param dbHL      the threshold intensity
     * @param ear       which ear
     */
    public void addThreshold(Frequency frequency, int dbHL, Ear ear) {
        getThresholdData(ear).addThreshold(frequency, dbHL);
    }

    /**
     * Returns the threshold data for the specified ear.
     *
     * @param ear RIGHT or LEFT
     * @return the ThresholdData for that ear
     */
    public ThresholdData getThresholdData(Ear ear) {
        return ear == Ear.RIGHT ? rightEarData : leftEarData;
    }

    public ThresholdData getRightEarData() {
        return rightEarData;
    }

    public ThresholdData getLeftEarData() {
        return leftEarData;
    }

    public PatientInfo getPatient() {
        return patient;
    }

    /**
     * Returns an unmodifiable view of the complete test log.
     *
     * @return list of all TestResult entries
     */
    public List<TestResult> getTestLog() {
        return Collections.unmodifiableList(testLog);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Marks the session as complete and records the end time.
     */
    public void endSession() {
        this.endTime = LocalDateTime.now();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Returns the duration of the test session.
     *
     * @return duration between start and end (or start and now if not ended)
     */
    public Duration getSessionDuration() {
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    /**
     * Checks if both ears have been fully tested.
     *
     * @return true if all frequencies tested for both ears
     */
    public boolean isComplete() {
        return rightEarData.isComplete() && leftEarData.isComplete();
    }

    /**
     * Returns a summary of the session progress.
     *
     * @return formatted summary string
     */
    public String getProgressSummary() {
        return String.format("Right Ear: %d/%d | Left Ear: %d/%d | Total presentations: %d",
                rightEarData.getTestedCount(), Frequency.values().length,
                leftEarData.getTestedCount(), Frequency.values().length,
                testLog.size());
    }

    @Override
    public String toString() {
        return String.format("AudiogramSession[%s, %s]", patient.getFullName(), getProgressSummary());
    }
}
