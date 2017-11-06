/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.util.Map;

import org.freedesktop.NetworkManager;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Kristensen
 *
 */
public class NWMConnection implements INWMConnection {

    private static final Logger log = LoggerFactory.getLogger(NWMConnection.class);

    private NetworkManager.Settings.Connection nwmConnection;

    private Properties nwmConnectionProperties;

    private NetworkManager.Settings nwmConnectionSettings;

    private INWMProvider nwmProvider;

    private String connectionObjectPath;

    /**
     * The signal handler for changing properties.
     */
    private DBusSigHandler<NetworkManager.Settings.Connection.PropertiesChanged> propertiesChangedSignalHandler;

    private DBusSigHandler<NetworkManager.Settings.Connection.Removed> removedSignalHandler;

    private DBusSigHandler<NetworkManager.Settings.Connection.Updated> updatedSignalHandler;

    /**
     * Construct a new AccessPoint
     * 
     * @param nwmProvider
     * @param accessPointObjectPath
     */
    public NWMConnection(INWMProvider nwmProvider, String connectionObjectPath) {
        this.nwmProvider = nwmProvider;
        this.connectionObjectPath = connectionObjectPath;

        DBusConnection dbusConnection = this.nwmProvider.getDbusConnection();

        try {

            nwmConnection = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, connectionObjectPath,
                    NetworkManager.Settings.Connection.class);
            log.info("Got connection '{}'", nwmConnection);

            nwmConnectionProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, connectionObjectPath,
                    Properties.class);
            log.info("Got connection.Properties '{}'", nwmConnectionProperties);

            nwmConnectionSettings = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, connectionObjectPath,
                    NetworkManager.Settings.class);

            log.info("Got connection.Settings '{}'", nwmConnectionSettings);

        } catch (DBusException e) {
            log.error("Error constructing Connectin");
        }
    }

    @Override
    public void startup() throws Exception {

        /*
         * Setup propertiesChanged Signal Handler
         */
        propertiesChangedSignalHandler = new DBusSigHandler<NetworkManager.Settings.Connection.PropertiesChanged>() {

            @Override
            public void handle(NetworkManager.Settings.Connection.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        nwmProvider.getDbusConnection().addSigHandler(NetworkManager.Settings.Connection.PropertiesChanged.class,
                nwmProvider.getNWMDbusBusName(), nwmConnectionProperties, propertiesChangedSignalHandler);

        log.debug("Added connection sigHandler.PropertiesChanged ");

        /*
         * Setup propertiesChanged Signal Handler
         */
        removedSignalHandler = new DBusSigHandler<NetworkManager.Settings.Connection.Removed>() {

            @Override
            public void handle(NetworkManager.Settings.Connection.Removed removed) {
                handleRemovedDbusSignal(removed);
            }
        };

        nwmProvider.getDbusConnection().addSigHandler(NetworkManager.Settings.Connection.PropertiesChanged.class,
                nwmProvider.getNWMDbusBusName(), nwmConnectionProperties, propertiesChangedSignalHandler);

        log.debug("Added connection sigHandler.PropertiesChanged ");
        /*
         * Setup propertiesChanged Signal Handler
         */
        updatedSignalHandler = new DBusSigHandler<NetworkManager.Settings.Connection.Updated>() {

            @Override
            public void handle(NetworkManager.Settings.Connection.Updated updated) {
                handleUpdatedDbusSignal(updated);
            }
        };

        nwmProvider.getDbusConnection().addSigHandler(NetworkManager.Settings.Connection.PropertiesChanged.class,
                nwmProvider.getNWMDbusBusName(), nwmConnectionProperties, propertiesChangedSignalHandler);

        log.debug("Added connection sigHandler.PropertiesChanged ");

    }

    @Override
    public void shutdown() throws Exception {
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Settings.Connection.PropertiesChanged.class,
                propertiesChangedSignalHandler);
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Settings.Connection.Updated.class,
                updatedSignalHandler);
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Settings.Connection.Removed.class,
                removedSignalHandler);
    }

    /**
     * Handle a DBus signal for properties changes on the device.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handlePropertiesChangedDbusSignal(
            final NetworkManager.Settings.Connection.PropertiesChanged propertiesChanged) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("Properties changed for Connection {}", propertiesChanged.properties);
            }
        };
        new Thread(run).start();
    }

    /**
     * Handle a DBus signal for connection updated.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handleUpdatedDbusSignal(final NetworkManager.Settings.Connection.Updated updated) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("Connection updated {}", updated);
            }
        };
        new Thread(run).start();
    }

    /**
     * Handle a DBus signal for connection removed.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handleRemovedDbusSignal(final NetworkManager.Settings.Connection.Removed removed) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("Connection removed: {}", removed);
            }
        };
        new Thread(run).start();
    }

    @Override
    public String getConnectionObjectPath() {
        return connectionObjectPath;
    }

    @Override
    public NetworkManager.Settings getSettings() {
        return nwmConnectionSettings;
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getSsid()
     */

    @Override
    public NetworkManager.Settings.Connection getConnection() {
        return nwmConnection;
    }

    @Override
    public boolean getUnsaved() {

        Variant<Boolean> value = nwmConnectionProperties.Get(NWM_CONNECTION_INTERFACE, NWM_CONNECTION_UNSAVED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getAll()
     */
    @Override
    public Map<String, Variant<?>> getAll() {
        return nwmConnectionProperties.GetAll(NWM_ACCESSPOINT_INTERFACE);
    }

}
