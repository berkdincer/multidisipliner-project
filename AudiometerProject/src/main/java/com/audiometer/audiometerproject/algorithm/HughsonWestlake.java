package com.audiometer.audiometerproject.algorithm;

/**
 * Implements the Hughson-Westlake modified ascending procedure
 * for determining pure-tone hearing thresholds.
 *
 * <h3>Algorithm Rules (per IEC 60645-1 / ASHA guidelines):</h3>
 * <ul>
 *   <li>Start at 30 dBHL</li>
 *   <li>If patient <b>hears</b> the tone → decrease by 10 dB (descending)</li>
 *   <li>If patient <b>does not hear</b> → increase by 5 dB (ascending)</li>
 *   <li>Threshold = the lowest level at which patient responds
 *       in at least 2 out of 3 ascending presentations at the same level</li>
 *   <li>Intensity range: -10 dBHL to 120 dBHL</li>
 * </ul>
 *
 * <p>This class implements the {@link TestAlgorithm} interface,
 * demonstrating <b>polymorphism</b> and the <b>Strategy pattern</b>.</p>
 */
public class HughsonWestlake implements TestAlgorithm {

    // --- Configuration Constants ---
    private static final int DEFAULT_START_DB = 30;
    private static final int DESCEND_STEP = 10;  // dB to decrease when heard
    private static final int ASCEND_STEP = 5;    // dB to increase when not heard
    private static final int MIN_DB = -10;
    private static final int MAX_DB = 120;
    private static final int REQUIRED_RESPONSES = 2;    // must hear 2 out of 3
    private static final int REQUIRED_PRESENTATIONS = 3; // at the same ascending level

    // --- State Variables ---
    private int currentIntensity;
    private int startingIntensity;
    private TestState currentState;

    /**
     * Tracks the number of ascending presentations at each intensity level.
     * Key: intensity (dBHL), Value: count of ascending presentations.
     */
    private int ascendingCount;

    /**
     * Tracks the number of positive responses at the current ascending level.
     */
    private int responseCount;

    /**
     * The intensity level currently being evaluated for threshold.
     */
    private int candidateLevel;

    /**
     * Whether the last direction was ascending (came from below).
     */
    private boolean lastWasAscending;

    /**
     * The determined threshold value (-1 if not yet found).
     */
    private int thresholdDb;

    /**
     * Whether the very first tone has been presented.
     */
    private boolean firstPresentation;

    /**
     * Creates a HughsonWestlake algorithm with the default starting intensity (30 dB).
     */
    public HughsonWestlake() {
        this(DEFAULT_START_DB);
    }

    /**
     * Creates a HughsonWestlake algorithm with a custom starting intensity.
     *
     * @param startingDb the initial intensity in dBHL
     */
    public HughsonWestlake(int startingDb) {
        this.startingIntensity = startingDb;
        reset();
    }

    @Override
    public void reset() {
        this.currentIntensity = startingIntensity;
        this.currentState = TestState.IDLE;
        this.ascendingCount = 0;
        this.responseCount = 0;
        this.candidateLevel = Integer.MIN_VALUE;
        this.lastWasAscending = false;
        this.thresholdDb = -1;
        this.firstPresentation = true;
    }

    @Override
    public int getNextIntensity() {
        currentState = TestState.PRESENTING_TONE;
        return currentIntensity;
    }

    @Override
    public void recordResponse(boolean heard) {
        currentState = TestState.WAITING_RESPONSE;

        if (firstPresentation) {
            firstPresentation = false;
            if (heard) {
                // Patient heard the first tone → descend
                currentIntensity = clamp(currentIntensity - DESCEND_STEP);
                currentState = TestState.DESCENDING;
                lastWasAscending = false;
            } else {
                // Patient did not hear → ascend
                currentIntensity = clamp(currentIntensity + ASCEND_STEP);
                currentState = TestState.ASCENDING;
                lastWasAscending = true;
            }
            return;
        }

        if (heard) {
            // --- Patient HEARD the tone ---
            if (lastWasAscending) {
                // This is an ascending approach — counts toward threshold
                if (currentIntensity != candidateLevel) {
                    // New candidate level
                    candidateLevel = currentIntensity;
                    ascendingCount = 1;
                    responseCount = 1;
                } else {
                    // Same level, increment counters
                    ascendingCount++;
                    responseCount++;
                }

                // Check threshold criterion: 2 out of 3 ascending responses
                if (responseCount >= REQUIRED_RESPONSES) {
                    thresholdDb = currentIntensity;
                    currentState = TestState.THRESHOLD_FOUND;
                    return;
                }

                if (ascendingCount >= REQUIRED_PRESENTATIONS) {
                    // 3 presentations done but less than 2 responses
                    // This shouldn't happen if responseCount >= 2 is checked,
                    // but reset for this level if needed
                    if (responseCount < REQUIRED_RESPONSES) {
                        // Not enough responses — continue ascending
                        currentIntensity = clamp(currentIntensity + ASCEND_STEP);
                        currentState = TestState.ASCENDING;
                        candidateLevel = Integer.MIN_VALUE;
                        ascendingCount = 0;
                        responseCount = 0;
                        lastWasAscending = true;
                        return;
                    }
                }
            }

            // Descend after hearing
            currentIntensity = clamp(currentIntensity - DESCEND_STEP);
            currentState = TestState.DESCENDING;
            lastWasAscending = false;

        } else {
            // --- Patient DID NOT hear the tone ---
            if (lastWasAscending && currentIntensity == candidateLevel) {
                ascendingCount++;
                // Check if we've exhausted presentations at this level
                if (ascendingCount >= REQUIRED_PRESENTATIONS && responseCount < REQUIRED_RESPONSES) {
                    // Failed to reach threshold at this level, move up
                    candidateLevel = Integer.MIN_VALUE;
                    ascendingCount = 0;
                    responseCount = 0;
                }
            }

            // Ascend after not hearing
            currentIntensity = clamp(currentIntensity + ASCEND_STEP);
            currentState = TestState.ASCENDING;
            lastWasAscending = true;

            // Safety check: if we've hit maximum intensity
            if (currentIntensity >= MAX_DB) {
                thresholdDb = MAX_DB;
                currentState = TestState.THRESHOLD_FOUND;
            }
        }
    }

    @Override
    public boolean isThresholdFound() {
        return currentState == TestState.THRESHOLD_FOUND;
    }

    @Override
    public int getThresholdDb() {
        if (!isThresholdFound()) {
            throw new IllegalStateException("Threshold has not been determined yet.");
        }
        return thresholdDb;
    }

    @Override
    public TestState getCurrentState() {
        return currentState;
    }

    @Override
    public int getStartingIntensity() {
        return startingIntensity;
    }

    /**
     * Returns the current intensity being tested.
     *
     * @return current intensity in dBHL
     */
    public int getCurrentIntensity() {
        return currentIntensity;
    }

    /**
     * Clamps the intensity value to the valid range [-10, 120] dBHL.
     *
     * @param db the intensity to clamp
     * @return clamped value
     */
    private int clamp(int db) {
        return Math.max(MIN_DB, Math.min(MAX_DB, db));
    }

    @Override
    public String toString() {
        return String.format("HughsonWestlake[intensity=%d dB, state=%s, candidate=%d, responses=%d/%d]",
                currentIntensity, currentState, candidateLevel, responseCount, ascendingCount);
    }
}
