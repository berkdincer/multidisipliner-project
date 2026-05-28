package com.audiometer.audiometerproject.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Concrete implementation of {@link SerialCommunicator} using the jSerialComm library.
 * Handles the low-level serial port operations: opening, reading, writing, and closing.
 *
 * <p>Incoming data is buffered character by character until a complete "RESPONSE"
 * keyword is detected, at which point the registered {@link ResponseListener} is notified.</p>
 *
 * <p><b>OOP Principles:</b></p>
 * <ul>
 *   <li>Implements {@link SerialCommunicator} interface (Polymorphism)</li>
 *   <li>Encapsulates jSerialComm details (Encapsulation)</li>
 *   <li>Uses Observer pattern via {@link ResponseListener}</li>
 * </ul>
 */
public class JSerialCommAdapter implements SerialCommunicator {

    private SerialPort serialPort;
    private ResponseListener responseListener;
    private final StringBuilder messageBuffer = new StringBuilder();

    @Override
    public boolean connect(String portName, int baudRate) {
        try {
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(baudRate);

            if (serialPort.openPort()) {
                System.out.println("[Serial] Connected to " + portName + " at " + baudRate + " bps");
                setupDataListener();
                return true;
            } else {
                System.err.println("[Serial] Failed to open port: " + portName);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[Serial] Connection error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
            System.out.println("[Serial] Disconnected.");
        }
    }

    @Override
    public void sendCommand(String command) {
        if (serialPort != null && serialPort.isOpen()) {
            byte[] bytes = (command + "\n").getBytes();
            int written = serialPort.writeBytes(bytes, bytes.length);
            System.out.println("[Serial] Sent: " + command + " (" + written + " bytes)");
        } else {
            System.err.println("[Serial] Cannot send — port not connected.");
        }
    }

    @Override
    public void setResponseListener(ResponseListener listener) {
        this.responseListener = listener;
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    @Override
    public String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            names[i] = ports[i].getSystemPortName();
        }
        return names;
    }

    /**
     * Sets up the asynchronous data listener on the serial port.
     * Buffers incoming bytes and checks for the "RESPONSE" keyword.
     */
    private void setupDataListener() {
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }

                int available = serialPort.bytesAvailable();
                if (available <= 0) return;

                byte[] newData = new byte[available];
                int numRead = serialPort.readBytes(newData, newData.length);
                String receivedChars = new String(newData, 0, numRead);

                // Buffer incoming characters
                messageBuffer.append(receivedChars);

                // Check if buffer contains the RESPONSE keyword
                if (messageBuffer.toString().contains("RESPONSE")) {
                    System.out.println("[Serial] RESPONSE received from hardware!");

                    if (responseListener != null) {
                        responseListener.onResponseReceived("RESPONSE");
                    }

                    // Clear buffer for next message
                    messageBuffer.setLength(0);
                }
            }
        });
    }
}
