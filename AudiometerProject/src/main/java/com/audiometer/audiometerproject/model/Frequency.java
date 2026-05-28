package com.audiometer.audiometerproject.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the standard audiometric test frequencies.
 * Range: 250 Hz to 8000 Hz (octave intervals).
 * These are the frequencies defined by the IEC 60645-1 standard
 * for pure-tone audiometry.
 */
public enum Frequency {
    HZ_250(250),
    HZ_500(500),
    HZ_1000(1000),
    HZ_2000(2000),
    HZ_4000(4000),
    HZ_8000(8000);

    private final int value;

    Frequency(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric frequency value in Hertz.
     * @return frequency in Hz
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a formatted display string (e.g. "1000 Hz").
     * @return formatted frequency string
     */
    public String getDisplayString() {
        return value + " Hz";
    }

    /**
     * Returns the short label used on audiogram X-axis (e.g. "1k", "250").
     * @return short label
     */
    public String getShortLabel() {
        if (value >= 1000) {
            return (value / 1000) + "k";
        }
        return String.valueOf(value);
    }

    /**
     * Returns all frequencies in ascending order.
     * @return ordered list of all Frequency values
     */
    public static List<Frequency> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Finds a Frequency enum by its numeric Hz value.
     * @param hz the frequency in Hertz
     * @return the matching Frequency enum
     * @throws IllegalArgumentException if no match found
     */
    public static Frequency fromValue(int hz) {
        for (Frequency f : values()) {
            if (f.value == hz) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unsupported frequency: " + hz + " Hz");
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
