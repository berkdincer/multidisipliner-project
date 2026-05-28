package com.audiometer.audiometerproject.model;

import javafx.scene.paint.Color;

/**
 * Represents which ear is being tested in an audiometric examination.
 * Follows clinical audiology standards for symbols and colors:
 * - Right ear: Red circle (O)
 * - Left ear: Blue cross (X)
 */
public enum Ear {
    RIGHT("Right Ear", "O", Color.RED),
    LEFT("Left Ear", "X", Color.BLUE);

    private final String displayName;
    private final String symbol;
    private final Color color;

    Ear(String displayName, String symbol, Color color) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
    }

    /**
     * Returns the clinical symbol used on audiograms.
     * @return "O" for right ear, "X" for left ear
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the standard audiogram color for this ear.
     * @return Red for right ear, Blue for left ear
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns a human-readable name for display in the UI.
     * @return "Right Ear" or "Left Ear"
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
