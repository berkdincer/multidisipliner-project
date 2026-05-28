package com.audiometer.audiometerproject.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the hearing threshold values for a single ear.
 * A threshold is the minimum intensity (in dBHL) at which the patient
 * can detect a tone at a given frequency.
 *
 * Thresholds are stored as a map of Frequency → dBHL value.
 */
public class ThresholdData {
    private final Ear ear;
    private final Map<Frequency, Integer> thresholds;

    /**
     * Creates a new ThresholdData container for the specified ear.
     *
     * @param ear which ear this data belongs to
     */
    public ThresholdData(Ear ear) {
        this.ear = ear;
        this.thresholds = new LinkedHashMap<>();
    }

    /**
     * Records a threshold value for a given frequency.
     * Overwrites any previous value at that frequency.
     *
     * @param frequency the test frequency
     * @param dbHL      the threshold intensity in dBHL
     */
    public void addThreshold(Frequency frequency, int dbHL) {
        thresholds.put(frequency, dbHL);
    }

    /**
     * Returns the threshold at a given frequency, or null if not yet tested.
     *
     * @param frequency the frequency to query
     * @return threshold in dBHL, or null
     */
    public Integer getThreshold(Frequency frequency) {
        return thresholds.get(frequency);
    }

    /**
     * Checks whether a threshold has been recorded for the given frequency.
     *
     * @param frequency the frequency to check
     * @return true if a threshold exists
     */
    public boolean hasThreshold(Frequency frequency) {
        return thresholds.containsKey(frequency);
    }

    /**
     * Returns an unmodifiable view of all recorded thresholds.
     *
     * @return map of Frequency → dBHL
     */
    public Map<Frequency, Integer> getAllThresholds() {
        return Collections.unmodifiableMap(thresholds);
    }

    /**
     * Returns which ear this threshold data belongs to.
     *
     * @return the ear (RIGHT or LEFT)
     */
    public Ear getEar() {
        return ear;
    }

    /**
     * Returns the number of frequencies that have been tested.
     *
     * @return count of recorded thresholds
     */
    public int getTestedCount() {
        return thresholds.size();
    }

    /**
     * Checks if all standard frequencies have been tested.
     *
     * @return true if all 6 frequencies have threshold values
     */
    public boolean isComplete() {
        return thresholds.size() == Frequency.values().length;
    }

    /**
     * Removes all recorded thresholds.
     */
    public void clear() {
        thresholds.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ear.getDisplayName()).append(" Thresholds:\n");
        for (Frequency f : Frequency.getAll()) {
            Integer val = thresholds.get(f);
            sb.append("  ").append(f.getDisplayString()).append(": ");
            sb.append(val != null ? val + " dBHL" : "Not tested");
            sb.append("\n");
        }
        return sb.toString();
    }
}
