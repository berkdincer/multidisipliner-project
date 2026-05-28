package com.audiometer.audiometerproject.serial;

/**
 * Abstraction for serial port communication.
 * Decouples the application from any specific serial library (e.g., jSerialComm).
 *
 * <p><b>Interface Segregation & Dependency Inversion:</b>
 * The UI/controller layer depends on this interface, not on jSerialComm directly.
 * This makes the code testable and allows swapping implementations.</p>
 */
public interface SerialCommunicator {

    /**
     * Opens a connection to the specified serial port.
     *
     * @param portName the system port name (e.g., "COM2", "/dev/ttyUSB0")
     * @param baudRate the communication speed (e.g., 2400)
     * @return true if connection was successful
     */
    boolean connect(String portName, int baudRate);

    /**
     * Closes the serial port connection.
     */
    void disconnect();

    /**
     * Sends a command string to the connected hardware.
     * A newline character is automatically appended.
     *
     * @param command the command to send (e.g., "F:1000,V:30")
     */
    void sendCommand(String command);

    /**
     * Registers a listener to be notified when a response is received.
     *
     * @param listener the callback for incoming messages
     */
    void setResponseListener(ResponseListener listener);

    /**
     * Checks whether the serial port is currently connected and open.
     *
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Returns the names of all available serial ports on the system.
     *
     * @return array of port names
     */
    String[] getAvailablePorts();
}
