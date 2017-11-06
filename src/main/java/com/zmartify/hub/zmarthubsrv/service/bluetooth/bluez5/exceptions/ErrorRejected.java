/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5.exceptions;

import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author Peter Kristensen
 *
 */
public class ErrorRejected extends DBusException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ErrorRejected(String message) {
        super(message);
    }

}
