package com.audiometer.audiometerproject.algorithm;

/**
 * Interface defining the contract for an audiometric test algorithm.
 * Different algorithms (e.g., Hughson-Westlake, Bekesy) can implement
 * this interface to provide different threshold search strategies.
 *
 * <p>This follows the Strategy pattern, allowing the controller
 * to work with any algorithm implementation interchangeably.</p>
 *
 * <p><b>Polymorphism:</b> The controller depends on this interface,
 * not on a concrete class. New algorithms can be added without
 * modifying existing code (Open/Closed Principle).</p>
 */
public interface TestAlgorithm {

    /**
     * Returns the intensity level (dBHL) for the next tone presentation.
     *
     * @return the intensity in dBHL to present
     */
    int getNextIntensity();

    /**
     * Records whether the patient responded to the current tone.
     *
     * @param heard true if the patient pressed the response button
     */
    void recordResponse(boolean heard);

    /**
     * Checks if the hearing threshold has been determined
     * for the current frequency.
     *
     * @return true if threshold is found
     */
    boolean isThresholdFound();

    /**
     * Returns the determined threshold value.
     * Should only be called after {@link #isThresholdFound()} returns true.
     *
     * @return threshold in dBHL
     * @throws IllegalStateException if threshold has not been found yet
     */
    int getThresholdDb();

    /**
     * Returns the current state of the algorithm.
     *
     * @return current TestState
     */
    TestState getCurrentState();

    /**
     * Resets the algorithm for testing a new frequency.
     * Clears all internal counters and returns to initial state.
     */
    void reset();

    /**
     * Returns the starting intensity for the test.
     *
     * @return starting dBHL value
     */
    int getStartingIntensity();
}
