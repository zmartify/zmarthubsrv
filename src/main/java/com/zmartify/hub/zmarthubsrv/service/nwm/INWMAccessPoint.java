/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.Map;

import org.freedesktop.NetworkManager.AccessPoint;
import org.freedesktop.NetworkManager.Settings;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;

/**
 * @author Peter Kristensen
 *
 */
public interface INWMAccessPoint {

    byte[] getSsid();

    byte getStrength();

    short getLastSeen();

    String getHwAddress();

    UInt32 getFlags();

    UInt32 getFrequency();

    UInt32 getMaxBitRate();

    UInt32 getMode();

    UInt32 getRsnFlags();

    UInt32 getWpaFlags();

    Map<String, Variant<?>> getAll();

    /**
     * @throws Exception
     */
    void startup() throws Exception;

    /**
     * @throws Exception
     */
    void shutdown() throws Exception;

    /**
     * @return
     */
    Settings getSettings();

    /**
     * @return
     */
    AccessPoint getAccessPoint();

    /**
     * @return
     */
    String getAccessPointObjectPath();

}
