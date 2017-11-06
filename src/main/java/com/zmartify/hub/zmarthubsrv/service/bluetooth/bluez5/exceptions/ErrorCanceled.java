/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5.exceptions;

import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @author Peter Kristensen
 *
 */
public class ErrorCanceled extends DBusException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ErrorCanceled(String message) {
        super(message);
    }
}
