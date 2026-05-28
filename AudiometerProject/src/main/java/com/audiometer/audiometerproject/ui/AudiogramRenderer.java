package com.audiometer.audiometerproject.ui;

import com.audiometer.audiometerproject.model.Ear;
import com.audiometer.audiometerproject.model.Frequency;
import com.audiometer.audiometerproject.model.ThresholdData;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Map;

/**
 * Custom Canvas-based audiogram renderer that draws a clinical-standard audiogram.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Standard audiogram grid (125–8000 Hz, -10 to 120 dBHL)</li>
 *   <li>Right ear: Red "O" symbols</li>
 *   <li>Left ear: Blue "X" symbols</li>
 *   <li>Connecting lines between threshold points</li>
 *   <li>Proper axis labels and gridlines</li>
 * </ul>
 *
 * <p><b>Single Responsibility:</b> This class only handles audiogram drawing.
 * Data storage and algorithm logic are in separate classes.</p>
 */
public class AudiogramRenderer {

    // --- Layout Constants ---
    private static final double MARGIN_LEFT = 70;
    private static final double MARGIN_RIGHT = 30;
    private static final double MARGIN_TOP = 40;
    private static final double MARGIN_BOTTOM = 50;

    // --- Audiogram Range ---
    private static final int DB_MIN = -10;
    private static final int DB_MAX = 120;
    private static final int DB_STEP = 10;

    // --- Frequency Labels (for X-axis positions) ---
    private static final int[] FREQ_VALUES = {125, 250, 500, 1000, 2000, 4000, 8000};
    private static final String[] FREQ_LABELS = {"125", "250", "500", "1k", "2k", "4k", "8k"};

    // --- Colors ---
    private static final Color GRID_COLOR = Color.rgb(200, 200, 200);
    private static final Color GRID_MAJOR_COLOR = Color.rgb(160, 160, 160);
    private static final Color AXIS_COLOR = Color.rgb(60, 60, 60);
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color RIGHT_EAR_COLOR = Color.rgb(220, 50, 50);   // Red
    private static final Color LEFT_EAR_COLOR = Color.rgb(40, 80, 200);    // Blue
    private static final Color NORMAL_ZONE_COLOR = Color.rgb(230, 245, 230, 0.5); // Light green

    // --- Symbol Size ---
    private static final double SYMBOL_SIZE = 14;

    private final Canvas canvas;

    /**
     * Creates an AudiogramRenderer bound to the given Canvas.
     *
     * @param canvas the JavaFX Canvas to draw on
     */
    public AudiogramRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Draws the complete audiogram with grid, labels, and data points.
     *
     * @param rightEar threshold data for the right ear (may be null)
     * @param leftEar  threshold data for the left ear (may be null)
     */
    public void draw(ThresholdData rightEar, ThresholdData leftEar) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear canvas
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        // Draw components
        drawNormalHearingZone(gc, w, h);
        drawGrid(gc, w, h);
        drawAxes(gc, w, h);
        drawTitle(gc, w);

        // Plot data
        if (rightEar != null) {
            drawThresholdLine(gc, rightEar, w, h);
            drawThresholdPoints(gc, rightEar, w, h);
        }
        if (leftEar != null) {
            drawThresholdLine(gc, leftEar, w, h);
            drawThresholdPoints(gc, leftEar, w, h);
        }

        // Draw legend
        drawLegend(gc, w, h);
    }

    /**
     * Highlights the normal hearing zone (0–25 dBHL).
     */
    private void drawNormalHearingZone(GraphicsContext gc, double w, double h) {
        double plotW = w - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = h - MARGIN_TOP - MARGIN_BOTTOM;

        double y0 = MARGIN_TOP + dbToY(0, plotH);
        double y25 = MARGIN_TOP + dbToY(25, plotH);

        gc.setFill(NORMAL_ZONE_COLOR);
        gc.fillRect(MARGIN_LEFT, y0, plotW, y25 - y0);
    }

    /**
     * Draws the grid lines.
     */
    private void drawGrid(GraphicsContext gc, double w, double h) {
        double plotW = w - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = h - MARGIN_TOP - MARGIN_BOTTOM;

        gc.setLineWidth(0.5);

        // Horizontal grid lines (dB levels)
        for (int db = DB_MIN; db <= DB_MAX; db += DB_STEP) {
            double y = MARGIN_TOP + dbToY(db, plotH);
            gc.setStroke(db % 20 == 0 ? GRID_MAJOR_COLOR : GRID_COLOR);
            gc.strokeLine(MARGIN_LEFT, y, w - MARGIN_RIGHT, y);
        }

        // Vertical grid lines (frequencies)
        for (int i = 0; i < FREQ_VALUES.length; i++) {
            double x = MARGIN_LEFT + freqToX(i, plotW);
            gc.setStroke(GRID_COLOR);
            gc.strokeLine(x, MARGIN_TOP, x, h - MARGIN_BOTTOM);
        }
    }

    /**
     * Draws the axes with labels.
     */
    private void drawAxes(GraphicsContext gc, double w, double h) {
        double plotW = w - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = h - MARGIN_TOP - MARGIN_BOTTOM;

        gc.setStroke(AXIS_COLOR);
        gc.setLineWidth(1.5);

        // X-axis (top)
        gc.strokeLine(MARGIN_LEFT, MARGIN_TOP, w - MARGIN_RIGHT, MARGIN_TOP);
        // Y-axis (left)
        gc.strokeLine(MARGIN_LEFT, MARGIN_TOP, MARGIN_LEFT, h - MARGIN_BOTTOM);
        // Bottom border
        gc.strokeLine(MARGIN_LEFT, h - MARGIN_BOTTOM, w - MARGIN_RIGHT, h - MARGIN_BOTTOM);
        // Right border
        gc.strokeLine(w - MARGIN_RIGHT, MARGIN_TOP, w - MARGIN_RIGHT, h - MARGIN_BOTTOM);

        // Frequency labels (X-axis, bottom)
        gc.setFill(AXIS_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < FREQ_VALUES.length; i++) {
            double x = MARGIN_LEFT + freqToX(i, plotW);
            gc.fillText(FREQ_LABELS[i], x, h - MARGIN_BOTTOM + 16);
        }
        // X-axis title
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Frequency (Hz)", w / 2, h - 5);

        // dB labels (Y-axis, left)
        gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int db = DB_MIN; db <= DB_MAX; db += DB_STEP) {
            double y = MARGIN_TOP + dbToY(db, plotH);
            gc.fillText(String.valueOf(db), MARGIN_LEFT - 8, y + 4);
        }

        // Y-axis title
        gc.save();
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.translate(15, h / 2);
        gc.rotate(-90);
        gc.fillText("Hearing Level (dBHL)", 0, 0);
        gc.restore();
    }

    /**
     * Draws the chart title.
     */
    private void drawTitle(GraphicsContext gc, double w) {
        gc.setFill(AXIS_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("AUDIOGRAM", w / 2, 25);
    }

    /**
     * Draws connecting lines between threshold points for one ear.
     */
    private void drawThresholdLine(GraphicsContext gc, ThresholdData data, double w, double h) {
        double plotW = w - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = h - MARGIN_TOP - MARGIN_BOTTOM;

        Color lineColor = data.getEar() == Ear.RIGHT ? RIGHT_EAR_COLOR : LEFT_EAR_COLOR;
        gc.setStroke(lineColor);
        gc.setLineWidth(1.5);

        if (data.getEar() == Ear.LEFT) {
            gc.setLineDashes(8, 4); // Dashed line for left ear
        } else {
            gc.setLineDashes(null); // Solid line for right ear
        }

        double prevX = -1, prevY = -1;

        for (int i = 0; i < FREQ_VALUES.length; i++) {
            int freqVal = FREQ_VALUES[i];
            Frequency freq = findFrequency(freqVal);
            if (freq == null) continue;

            Integer threshold = data.getThreshold(freq);
            if (threshold == null) continue;

            double x = MARGIN_LEFT + freqToX(i, plotW);
            double y = MARGIN_TOP + dbToY(threshold, plotH);

            if (prevX >= 0) {
                gc.strokeLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }

        gc.setLineDashes(null); // Reset
    }

    /**
     * Draws the symbols (O for right, X for left) at each threshold point.
     */
    private void drawThresholdPoints(GraphicsContext gc, ThresholdData data, double w, double h) {
        double plotW = w - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = h - MARGIN_TOP - MARGIN_BOTTOM;

        Ear ear = data.getEar();
        Color color = ear == Ear.RIGHT ? RIGHT_EAR_COLOR : LEFT_EAR_COLOR;
        String symbol = ear.getSymbol();

        gc.setFont(Font.font("System", FontWeight.BOLD, SYMBOL_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < FREQ_VALUES.length; i++) {
            int freqVal = FREQ_VALUES[i];
            Frequency freq = findFrequency(freqVal);
            if (freq == null) continue;

            Integer threshold = data.getThreshold(freq);
            if (threshold == null) continue;

            double x = MARGIN_LEFT + freqToX(i, plotW);
            double y = MARGIN_TOP + dbToY(threshold, plotH);

            if (ear == Ear.RIGHT) {
                // Draw circle "O"
                gc.setStroke(color);
                gc.setLineWidth(2.0);
                double r = SYMBOL_SIZE / 2.0;
                gc.strokeOval(x - r, y - r, SYMBOL_SIZE, SYMBOL_SIZE);
            } else {
                // Draw cross "X"
                gc.setStroke(color);
                gc.setLineWidth(2.0);
                double r = SYMBOL_SIZE / 2.0;
                gc.strokeLine(x - r, y - r, x + r, y + r);
                gc.strokeLine(x - r, y + r, x + r, y - r);
            }
        }
    }

    /**
     * Draws a legend showing the ear symbols.
     */
    private void drawLegend(GraphicsContext gc, double w, double h) {
        double legendX = w - MARGIN_RIGHT - 140;
        double legendY = MARGIN_TOP + 10;

        // Background
        gc.setFill(Color.rgb(255, 255, 255, 0.85));
        gc.fillRect(legendX, legendY, 130, 50);
        gc.setStroke(GRID_MAJOR_COLOR);
        gc.setLineWidth(1);
        gc.strokeRect(legendX, legendY, 130, 50);

        // Right ear
        gc.setStroke(RIGHT_EAR_COLOR);
        gc.setLineWidth(2);
        double symY1 = legendY + 17;
        gc.strokeOval(legendX + 10, symY1 - 6, 12, 12);
        gc.setFill(AXIS_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Right Ear (O)", legendX + 28, symY1 + 4);

        // Left ear
        gc.setStroke(LEFT_EAR_COLOR);
        double symY2 = legendY + 38;
        gc.strokeLine(legendX + 10, symY2 - 6, legendX + 22, symY2 + 6);
        gc.strokeLine(legendX + 10, symY2 + 6, legendX + 22, symY2 - 6);
        gc.setFill(AXIS_COLOR);
        gc.fillText("Left Ear (X)", legendX + 28, symY2 + 4);
    }

    // --- Coordinate Conversion Helpers ---

    /**
     * Converts a frequency index (0-6) to an X pixel position within the plot area.
     */
    private double freqToX(int freqIndex, double plotWidth) {
        return (freqIndex / (double) (FREQ_VALUES.length - 1)) * plotWidth;
    }

    /**
     * Converts a dBHL value to a Y pixel position within the plot area.
     * Note: Audiograms have higher dB values going downward.
     */
    private double dbToY(int db, double plotHeight) {
        return ((db - DB_MIN) / (double) (DB_MAX - DB_MIN)) * plotHeight;
    }

    /**
     * Finds the matching Frequency enum for a given Hz value, or null if not a test frequency.
     */
    private Frequency findFrequency(int hz) {
        try {
            return Frequency.fromValue(hz);
        } catch (IllegalArgumentException e) {
            return null; // 125 Hz is on the grid but not a test frequency
        }
    }

    /**
     * Exports the current canvas content to a snapshot image.
     *
     * @return a WritableImage of the audiogram, or null on error
     */
    public javafx.scene.image.WritableImage exportToImage() {
        return canvas.snapshot(null, null);
    }
}
