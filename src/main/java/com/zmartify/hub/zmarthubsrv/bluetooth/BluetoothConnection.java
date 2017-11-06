package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 * @author Peter Kristensen
 * 
 */
public class BluetoothConnection {

    private static Logger log = LoggerFactory.getLogger(BluetoothConnection.class);

    private ZmartBTDevice device;
    private StreamConnection streamConnection;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public BluetoothConnection(ZmartBTDevice device, StreamConnection streamConnection) {
        this.device = device;
        this.streamConnection = streamConnection;
        try {
            this.dataInputStream = streamConnection.openDataInputStream();
            this.dataOutputStream = streamConnection.openDataOutputStream();
        } catch (IOException e) {
            log.error("Error opening input and output streams");
        }
    }

    /**
     * Disconnects the dataInputStream and dataOutputStream
     */
    protected void disconnect() {
        log.debug("Disconnecting BluetoothConnection " + this + " ...");
        try {
            dataOutputStream.close();
        } catch (IOException e) {
            log.error("Failed to close output stream :: {}", e);
        }
        try {
            dataInputStream.close();
        } catch (IOException e) {
            log.error("Failed to close input stream :: {}", e);
        }
        try {
            streamConnection.close();
        } catch (IOException e) {
            log.error("Failed to close stream connection :: {}", e);
        }
        log.info("Bluetooth connection has been closed.");
    }

    private void handleSocketException(IOException e) throws IOException {
        log.warn("Got socket exception {}, we are disconnecting...", e.getMessage());
        this.disconnect();
        throw e;
    }

    @Override
    public String toString() {
        return "BTConnection[" + this.device.getReadableName() + "]";
    }

    /**
     * Writes the rawMessage, which always consists of and an 'int' (4 bytes)
     * specifying the length of the message, followed by the rawMessage.
     * 
     * @return boolean (true = success)
     * 
     * @throws IOException
     */
    public boolean writeRawMessage(byte[] rawMessage) throws IOException {
        try {
            dataOutputStream.writeShort(rawMessage.length);
            dataOutputStream.write(rawMessage);
            return true;
        } catch (IOException e) {
            handleSocketException(e);
            return false; // We will never get here!
        }
    }

    /**
     * Reads the rawPayload, which always consists of and an 'int' (4 bytes)
     * specifying the length of the message, followed by the rawPayload.
     * 
     * @return byte[] rawMessage
     * @throws IOException
     */
    public byte[] readRawMessage() throws IOException {
        try {
            int length = dataInputStream.readShort();
            log.info("Length: {}", length);
            byte[] rawMessage = new byte[length];

            dataInputStream.readFully(rawMessage, 0, length);
            return rawMessage;
        } catch (IOException e) {
            handleSocketException(e);
            return null; // We will never get here!
        }
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

}
