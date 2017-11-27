/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;

/**
 * @author Peter Kristensen
 *
 */
public interface INWMConnectionActive {

    Path getConnection();

    Path getSpecificObject();

    String getId();

    String getUuid();

    String getType();

    List<Path> getDevices();

    UInt32 getState();

    UInt32 getStatFlags();

    boolean getDefault();

    DBusInterface getIp4Config();

    DBusInterface getDhcp4Config();

    boolean getDefault6();

    DBusInterface getIp6Config();

    DBusInterface getDhcp6Config();

    boolean getVpn();

    DBusInterface getMaster();
    
    /**
     * @return
     */
    Map<String, Variant<?>> getAll();

    /**
     * @return
     */
    String getConnectionObjectPath();
}
