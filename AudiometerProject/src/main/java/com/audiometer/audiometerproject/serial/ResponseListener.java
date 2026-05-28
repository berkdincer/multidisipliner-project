package com.audiometer.audiometerproject.serial;

/**
 * Functional interface for receiving serial port responses.
 * Follows the Observer pattern — the serial communicator notifies
 * listeners when a complete message is received from hardware.
 */
@FunctionalInterface
public interface ResponseListener {
    /**
     * Called when a complete response message is received from the hardware.
     *
     * @param message the received message (e.g., "RESPONSE")
     */
    void onResponseReceived(String message);
}
