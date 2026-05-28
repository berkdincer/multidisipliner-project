package com.audiometer.audiometerproject.serial;

import com.audiometer.audiometerproject.model.Frequency;

/**
 * Utility class for building serial commands to send to the audiometer hardware.
 * Centralizes command formatting in one place (DRY principle).
 *
 * <p>Command format agreed with the Electrical Engineering team:
 * {@code F:<frequency>,V:<volume_dB>}</p>
 *
 * <p>Example: {@code F:1000,V:30} means play a 1000 Hz tone at 30 dBHL.</p>
 */
public final class SerialCommandBuilder {

    /** Private constructor — this is a utility class, not meant to be instantiated. */
    private SerialCommandBuilder() {
        throw new UnsupportedOperationException("Utility class — do not instantiate.");
    }

    /**
     * Builds a tone presentation command.
     *
     * @param frequency the frequency to play
     * @param intensityDb the intensity in dBHL
     * @return formatted command string (e.g., "F:1000,V:30")
     */
    public static String buildToneCommand(Frequency frequency, int intensityDb) {
        return "F:" + frequency.getValue() + ",V:" + intensityDb;
    }

    /**
     * Builds a tone presentation command from raw integer values.
     *
     * @param frequencyHz the frequency in Hz
     * @param intensityDb the intensity in dBHL
     * @return formatted command string
     */
    public static String buildToneCommand(int frequencyHz, int intensityDb) {
        return "F:" + frequencyHz + ",V:" + intensityDb;
    }

    /**
     * Builds a stop command to halt tone generation.
     *
     * @return "STOP" command string
     */
    public static String buildStopCommand() {
        return "STOP";
    }
}
