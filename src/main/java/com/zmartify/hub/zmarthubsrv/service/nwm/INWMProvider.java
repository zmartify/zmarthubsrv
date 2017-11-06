/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.List;
import java.util.Map;

import org.freedesktop.NetworkManager;
import org.freedesktop.NetworkManager.Settings;
import org.freedesktop.Pair;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Peter Kristensen
 *
 */
public interface INWMProvider {

    public void startup() throws Exception;

    public void shutdown() throws Exception;

    public List<DBusInterface> getDevices();

    public List<DBusInterface> getAllDevices();

    public boolean getNetworkingEnabled();

    public boolean getWirelessEnabled();

    public void setWirelessEnabled(boolean enabled);

    public boolean getWirelessHardwareEnabled();

    boolean getWwanEnabled();

    void setWwanEnabled(boolean enabled);

    public boolean getWimaxEnabled();

    public void setWimaxEnabled(boolean enabled);

    public List<Path> getActiveConnections();

    public Path getPrimaryConnection();

    public String getPrimaryConnectionType();

    public UInt32 getMetered();

    public Path getActivatingConnection();

    public boolean getStartup();

    public String getVerion();

    public UInt32 getState();

    public UInt32 getConnectivity();

    public Map<String, Variant<?>> getGlobalDnsConfiguration();

    public void setGlobalDnsConfiguration(Map<String, Variant<?>> configuration);

    /**
     * @return
     */
    public DBusConnection getDbusConnection();

    /**
     * @return
     */
    public String getNWMDbusBusName();

    /**
     * @return
     * @throws Exception
     */
    public List<String> listDevicesWireless() throws Exception;

    /**
     * @return
     */
    public Settings getNWMSettings();

    /**
     * @return
     */
    public NetworkManager getNetWorkManager();

    public Pair<DBusInterface, DBusInterface> connectToAP(String objectPath, String password) throws Exception;

    /**
     * @param devicePath
     * @return
     */
    public Map<String, Variant<?>> getDeviceProperties(Path devicePath);

    /**
     * @param uuid
     * @return
     */
    public DBusInterface getConnectionByUUID(String uuid);

    /**
     * @param connection
     * @return
     */
    public DBusInterface addConnectionUnSaved(Map<String, Map<String, Variant<?>>> connection);

    /**
     * @param objectPath
     * @return
     */
    public Map<String, Map<String, Variant<?>>> getConnectionSettings(String objectPath);

    /**
     * @return
     */
    public INWMDeviceWireless getWireless();

    /**
     * @return
     */
    public List<DBusInterface> listConnections();

    /**
     * @return
     */
    public boolean reloadConnections();

    /**
     * @param connection
     * @return
     */
    public DBusInterface addConnection(Map<String, Map<String, Variant<?>>> connection);

    /**
     * @param hostname
     */
    public void saveHostname(String hostname);

    /**
     * @return
     */
    ObjectMapper getJsonMapper();

}
