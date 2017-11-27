/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.List;

import org.freedesktop.NetworkManager;
import org.freedesktop.NetworkManager.Settings;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

import com.zmartify.hub.zmarthubsrv.service.nwm.NWMClass.NMDeviceWifiCapabilities;

/**
 * @author Peter Kristensen
 *
 */
public interface INWMDeviceWireless {

    /**
     * @throws DBusException
     * @throws DBusExecutionException
     */
    void startup() throws DBusExecutionException, DBusException;

    /**
     * @throws DBusException
     */
    void shutdown() throws DBusException;

    String getHwAddress();

    String getPermHwAddress();

    UInt32 getMode();

    UInt32 getBitrate();

    List<Path> getAccessPoints();

    List<ZmartAccessPoint> getAPs();

    List<ZmartAccessPoint> getActiveAPs();

    Path getActiveAccessPoint();

    UInt32 getWirelessCapabilities();

    /**
     * @return
     */
    List<NMDeviceWifiCapabilities> getWirelessCapabilitiesEnums();

    /**
     * @param objectPath
     * @return
     */
    ZmartAccessPoint getAP(String objectPath);

    /**
     * @return
     */
    NetworkManager.Device.Wireless getDeviceWireless();

    /**
     * @return
     */
    Settings getNWMSettings();

    /**
     * @return
     */
    Properties getNWMProperties();

    /**
     * @return
     */
    List<DBusInterface> getAllAccessPoints();

    /**
     * @return
     */
    String getDeviceWirelessObjectPath();
}
