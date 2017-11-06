/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freedesktop.DBus;
import org.freedesktop.NetworkManager;
import org.freedesktop.ObjectManager;
import org.freedesktop.Pair;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMClass.NMDeviceType;
import com.zmartify.hub.zmarthubsrv.utils.VariantSerializer;

/**
 * @author Peter Kristensen
 *
 */
public class NWMProvider implements INWMProvider {

    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The DBus connection used to talk to the Bluez service.
     */
    private DBusConnection dbusConnection = null;

    /**
     * The DBus ObjectManager for the root Bluez object.
     */
    private ObjectManager nwmObjectManager;

    private NetworkManager nwmNetworkManager;

    private Properties nwmProperties;

    private NetworkManager.Settings nwmSettings;

    private INWMDeviceWireless nwmWireless = null;

    private ObjectMapper jsonMapper;
    /**
     * The DBus signal handler for the ObjectManager's InterfacesAdded signal.
     */
    private DBusSigHandler<ObjectManager.InterfacesAdded> interfacesAddedSignalHandler;

    /**
     * The DBus signal handler for the ObjectManager's InterfacesRemoved signal.
     */
    private DBusSigHandler<ObjectManager.InterfacesRemoved> interfacesRemovedSignalHandler;

    /**
     * The unique name of the DBus bus for NetworkManager.
     */
    private String nwmDbusBusName;

    public NWMProvider() {
        jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new VariantSerializer(Variant.class));
        jsonMapper.registerModule(module);
    }

    @Override
    public void startup() throws DBusException {

        dbusConnection = DBusConnection.getConnection(DBusConnection.SYSTEM);

        DBus dbus = dbusConnection.getRemoteObject("org.freedesktop.DBus", "/", DBus.class);

        nwmDbusBusName = dbus.GetNameOwner(DBUS_NETWORKMANAGER);

        nwmObjectManager = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, "/org/freedesktop/NetworkManager",
                ObjectManager.class);
        /*
         * for (Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> object :
         * nwmObjectManager.GetManagedObjects() .entrySet()) { log.info("Object: {}",
         * object.getKey().getObjectPath()); }
         */
        nwmNetworkManager = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, "/org/freedesktop/NetworkManager",
                NetworkManager.class);

        nwmProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, "/org/freedesktop/NetworkManager",
                Properties.class);

        nwmSettings = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, "/org/freedesktop/NetworkManager/Settings",
                NetworkManager.Settings.class);

        interfacesAddedSignalHandler = new DBusSigHandler<ObjectManager.InterfacesAdded>() {
            @Override
            public void handle(ObjectManager.InterfacesAdded signal) {
                log.info("Interfaces added {} :: {}", signal.getObjectPath(), signal.getSig());
            }
        };

        interfacesRemovedSignalHandler = new DBusSigHandler<ObjectManager.InterfacesRemoved>() {

            @Override
            public void handle(ObjectManager.InterfacesRemoved signal) {
                log.info("Interfaces removed {} :: {}", signal.getObjectPath(), signal.getSig());
            }
        };

        dbusConnection.addSigHandler(ObjectManager.InterfacesAdded.class, nwmDbusBusName, nwmObjectManager,
                interfacesAddedSignalHandler);

        dbusConnection.addSigHandler(ObjectManager.InterfacesRemoved.class, nwmDbusBusName, nwmObjectManager,
                interfacesRemovedSignalHandler);

        /*
         * Loop through devices and set up the wireless Device
         */
        for (DBusInterface device : getAllDevices()) {
            Properties deviceProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, device.getObjectPath(),
                    Properties.class);
            Variant<UInt32> deviceType = deviceProperties.Get(NWM_DEVICE_INTERFACE, "DeviceType");

            if (NMDeviceType.NM_DEVICE_TYPE_WIFI.equals(deviceType.getValue())) {
                nwmWireless = new NWMDeviceWireless(this, device.getObjectPath());
                nwmWireless.startup();
                break;
            }
        }

        if (nwmWireless == null) {
            log.error("No wireless device found");
        }

        log.debug("NWM Provider started.");
    }

    @Override
    public void shutdown() throws DBusException {
        if (nwmWireless != null) {
            nwmWireless.shutdown();
            nwmWireless = null;
        }

        dbusConnection.removeSigHandler(ObjectManager.InterfacesAdded.class, interfacesAddedSignalHandler);
        dbusConnection.removeSigHandler(ObjectManager.InterfacesRemoved.class, interfacesRemovedSignalHandler);

        dbusConnection.disconnect();
        log.debug("NWM Provider shutdown.");
    }

    public List<DBusInterface> listAccessPoints() throws Exception {
        return nwmWireless.getDeviceWireless().GetAllAccessPoints();
    }

    @Override
    public List<String> listDevicesWireless() throws Exception {
        return getObjectsByInterface(NWM_DEVICEWIRELESS_INTERFACE);
    }

    /**
     * Get a list of all NetworkManager objects that implement a given DBus
     * interface.
     * 
     * @param objectInterface
     *            the name of the interface to scan for
     * 
     * @return a list of all objects implementing the specified interface
     */
    public List<String> getObjectsByInterface(String objectInterface) {
        List<String> results = new ArrayList<>();

        for (Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> objectByPath : nwmObjectManager
                .GetManagedObjects().entrySet()) {
            if (objectByPath.getValue().containsKey(objectInterface)) {
                results.add(objectByPath.getKey().toString());
            }
        }
        return results;
    }

    /**
     * Get the dbus connection.
     * 
     * @return the dbus connection
     */
    @Override
    public DBusConnection getDbusConnection() {
        return dbusConnection;
    }

    @Override
    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    /**
     * Get the unique DBus bus name for the NetworkManager service.
     * 
     * @return the NetworkManager DBus bus name
     */
    @Override
    public String getNWMDbusBusName() {
        return nwmDbusBusName;
    }

    @Override
    public NetworkManager getNetWorkManager() {
        return nwmNetworkManager;
    }

    @Override
    public NetworkManager.Settings getNWMSettings() {
        return nwmSettings;
    }

    @Override
    public INWMDeviceWireless getWireless() {
        return nwmWireless;
    }

    public void printManagedObjects() {
        log.info(nwmObjectManager.GetManagedObjects().toString());
    }

    @Override
    public List<DBusInterface> listConnections() {
        return nwmSettings.ListConnections();
    }

    @Override
    public boolean reloadConnections() {
        return nwmSettings.ReloadConnections();
    }

    @Override
    public DBusInterface addConnection(Map<String, Map<String, Variant<?>>> connection) {
        return nwmSettings.AddConnection(connection);
    }

    @Override
    public void saveHostname(String hostname) {
        nwmSettings.SaveHostname(hostname);
    }

    @Override
    public Map<String, Map<String, Variant<?>>> getConnectionSettings(String objectPath) {
        NetworkManager.Settings.Connection nwmSettingsConnection;
        try {
            nwmSettingsConnection = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, objectPath,
                    NetworkManager.Settings.Connection.class);
        } catch (DBusException e) {
            log.error("Unable to retrieve settings for connection {}", objectPath);
            return null;
        }
        return nwmSettingsConnection.GetSettings();
    }

    @Override
    public DBusInterface getConnectionByUUID(String uuid) {
        return nwmSettings.GetConnectionByUuid(uuid);
    }

    @Override
    public DBusInterface addConnectionUnSaved(Map<String, Map<String, Variant<?>>> connection) {
        return nwmSettings.AddConnectionUnsaved(connection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getDevices()
     */
    @Override
    public List<DBusInterface> getDevices() {
        Variant<List<DBusInterface>> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_DEVICES);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getAllDevices()
     */
    @Override
    public List<DBusInterface> getAllDevices() {
        return nwmNetworkManager.GetAllDevices();
        // Variant<List<DBusInterface>> value = nwmProperties.Get(NWM_INTERFACE,
        // NWM_PROPERTY_ALLDEVICES);
    }

    @Override
    public Map<String, Variant<?>> getDeviceProperties(Path devicePath) {
        return nwmProperties.GetAll(devicePath.getPath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getNetworkingEnabled(
     * )
     */
    @Override
    public boolean getNetworkingEnabled() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_NETWORKINGENABLED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getWirelessEnabled()
     */
    @Override
    public boolean getWirelessEnabled() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_WIRELESSENABLED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#setWirelessEnabled(
     * boolean)
     */
    @Override
    public void setWirelessEnabled(boolean enabled) {
        nwmProperties.Set(NWM_INTERFACE, NWM_PROPERTY_WIRELESSENABLED, new Variant<Boolean>(enabled));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#
     * getWirelessHardwareEnabled()
     */
    @Override
    public boolean getWirelessHardwareEnabled() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_WIRELESSHARDWAREENABLED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getWwanEnabled()
     */
    @Override
    public boolean getWwanEnabled() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_WWANENABLED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#setWwanEnabled(
     * boolean)
     */
    @Override
    public void setWwanEnabled(boolean enabled) {
        nwmProperties.Set(NWM_INTERFACE, NWM_PROPERTY_WWANENABLED, new Variant<Boolean>(enabled));

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getWimaxEnabled()
     */
    @Override
    public boolean getWimaxEnabled() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_WIMAXENABLED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#setWimaxEnabled(
     * boolean)
     */
    @Override
    public void setWimaxEnabled(boolean enabled) {
        nwmProperties.Set(NWM_INTERFACE, NWM_PROPERTY_WIMAXENABLED, new Variant<Boolean>(enabled));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getActiveConnections(
     * )
     */
    @Override
    public List<Path> getActiveConnections() {
        Variant<List<Path>> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_ACTIVECONNECTIONS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getPrimaryConnection(
     * )
     */
    @Override
    public Path getPrimaryConnection() {
        Variant<Path> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_PRIMARYCONNECTION);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#
     * getPrimaryConnectionType()
     */
    @Override
    public String getPrimaryConnectionType() {
        Variant<String> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_PRIMARYCONNECTIONTYPE);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getMetered()
     */
    @Override
    public UInt32 getMetered() {
        Variant<UInt32> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_METERED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#ActivatingConnection(
     * )
     */
    @Override
    public Path getActivatingConnection() {
        Variant<Path> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_ACTIVATINGCONNECTION);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getStartup()
     */
    @Override
    public boolean getStartup() {
        Variant<Boolean> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_STARTUP);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getVerion()
     */
    @Override
    public String getVerion() {
        Variant<String> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_VERSION);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getState()
     */
    @Override
    public UInt32 getState() {
        Variant<UInt32> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_STATE);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#getConnectivity()
     */
    @Override
    public UInt32 getConnectivity() {
        Variant<UInt32> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_CONNECTIVITY);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#
     * getGlobalDnsConfiguration()
     */
    @Override
    public Map<String, Variant<?>> getGlobalDnsConfiguration() {
        Variant<Map<String, Variant<?>>> value = nwmProperties.Get(NWM_INTERFACE, NWM_PROPERTY_GLOBALDNSCONFIGURATION);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMProvider#
     * setGlobalDnsConfiguration(java.util.Map)
     */
    @Override
    public void setGlobalDnsConfiguration(Map<String, Variant<?>> configuration) {
        nwmProperties.Set(NWM_INTERFACE, NWM_PROPERTY_WIMAXENABLED,
                new Variant<Map<String, Variant<?>>>(configuration));
    }

    @Override
    public Pair<DBusInterface, DBusInterface> connectToAP(String objectPath, String password) throws Exception {
        log.info("We got a request for new AccessPoint");
        NWMAccessPoint accessPoint = new NWMAccessPoint(this, objectPath);
        accessPoint.startup();
        String ssid = accessPoint.getSsidAsString();
        log.info("Ssid: {} - Password {}", ssid, password);
        DefaultWifiConnection connection = new DefaultWifiConnection(ssid, password);

        Pair<DBusInterface, DBusInterface> result = nwmNetworkManager.AddAndActivateConnection(connection.get(),
                nwmWireless.getDeviceWireless(), accessPoint.getAccessPoint());
        accessPoint.shutdown();
        Thread.sleep(20000);
        return result;
    }

}
