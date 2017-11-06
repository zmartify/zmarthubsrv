/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.Map;

import org.freedesktop.NetworkManager.Settings;
import org.freedesktop.NetworkManager.Settings.Connection;
import org.freedesktop.dbus.Variant;

/**
 * @author Peter Kristensen
 *
 */
public interface INWMConnection {

    /**
     * @throws Exception
     */
    void startup() throws Exception;

    /**
     * @throws Exception
     */
    void shutdown() throws Exception;

    boolean getUnsaved();

    /**
     * @return
     */
    Settings getSettings();

    /**
     * @return
     */
    Connection getConnection();

    /**
     * @return
     */
    Map<String, Variant<?>> getAll();

    /**
     * @return
     */
    String getConnectionObjectPath();
}
