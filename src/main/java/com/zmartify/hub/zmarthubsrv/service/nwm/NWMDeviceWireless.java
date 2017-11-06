/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.freedesktop.NetworkManager;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zmartify.hub.zmarthubsrv.service.nwm.NWMClass.NMDeviceWifiCapabilities;

/**
 * @author Peter Kristensen
 *
 */
public class NWMDeviceWireless implements INWMDeviceWireless {

    private static final Logger log = LoggerFactory.getLogger(NWMDeviceWireless.class);

    private NetworkManager.Device.Wireless nwmDeviceWireless;

    private Properties nwmDeviceWirelessProperties;

    private NetworkManager.Settings nwmDeviceWirelessSettings;

    private String deviceWirelessObjectPath;

    private INWMProvider nwmProvider;

    /**
     * The signal handlers for Device.Wireless.
     */
    private DBusSigHandler<NetworkManager.Device.Wireless.PropertiesChanged> propertiesChangedSignalHandler;
    private DBusSigHandler<NetworkManager.Device.Wireless.AccessPointAdded> accessPointAddedSignalHandler;
    private DBusSigHandler<NetworkManager.Device.Wireless.AccessPointRemoved> accessPointRemovedSignalHandler;
    private DBusSigHandler<NetworkManager.Device.Wireless.ScanDone> scanDoneSignalHandler;

    /**
     * Construct a new AccessPoint
     * 
     * @param nwmProvider
     * @param accessPointObjectPath
     */
    public NWMDeviceWireless(INWMProvider nwmProvider, String deviceWirelessObjectPath) {
        this.nwmProvider = nwmProvider;
        this.deviceWirelessObjectPath = deviceWirelessObjectPath;
        this.nwmProvider.getJsonMapper();

        DBusConnection dbusConnection = nwmProvider.getDbusConnection();

        try {
            nwmDeviceWireless = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, deviceWirelessObjectPath,
                    NetworkManager.Device.Wireless.class);

            log.debug("Got device '{}'", nwmDeviceWireless);

            nwmDeviceWirelessProperties = getProperties(deviceWirelessObjectPath);
            log.debug("Got deviceWireless.Properties '{}'", nwmDeviceWirelessProperties);

            nwmDeviceWirelessSettings = getSettings(deviceWirelessObjectPath);

            log.debug("Got deviceWireless.Settings '{}'", nwmDeviceWirelessSettings);

        } catch (DBusException e) {
            log.error("Error construction deviceWireless");
        }

    }

    @Override
    public void startup() throws DBusExecutionException, DBusException {
        DBusConnection dbusConnection = nwmProvider.getDbusConnection();
        /*
         * Setting up PropertiesChanged signal handler
         */
        propertiesChangedSignalHandler = new DBusSigHandler<NetworkManager.Device.Wireless.PropertiesChanged>() {
            @Override
            public void handle(NetworkManager.Device.Wireless.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        dbusConnection.addSigHandler(NetworkManager.Device.Wireless.PropertiesChanged.class,
                nwmProvider.getNWMDbusBusName(), nwmDeviceWirelessProperties, propertiesChangedSignalHandler);

        /*
         * Setting up AccessPointAdded signal handler
         */
        accessPointAddedSignalHandler = new DBusSigHandler<NetworkManager.Device.Wireless.AccessPointAdded>() {
            @Override
            public void handle(NetworkManager.Device.Wireless.AccessPointAdded accessPointAdded) {
                handleAccessPointAddedDbusSignal(accessPointAdded);
            }
        };

        dbusConnection.addSigHandler(NetworkManager.Device.Wireless.AccessPointAdded.class,
                nwmProvider.getNWMDbusBusName(), nwmDeviceWirelessProperties, accessPointAddedSignalHandler);

        log.debug("Added deviceWireless sigHandler.AccesPointAdded");

        /*
         * Setting up AccessPointRemoved signal handler
         */
        accessPointRemovedSignalHandler = new DBusSigHandler<NetworkManager.Device.Wireless.AccessPointRemoved>() {
            @Override
            public void handle(NetworkManager.Device.Wireless.AccessPointRemoved accessPointRemoved) {
                handleAccessPointRemovedDbusSignal(accessPointRemoved);
            }
        };

        dbusConnection.addSigHandler(NetworkManager.Device.Wireless.AccessPointRemoved.class,
                nwmProvider.getNWMDbusBusName(), nwmDeviceWirelessProperties, accessPointRemovedSignalHandler);

        log.debug("Added deviceWireless sigHandler.AccesPointRemoved");

        /*
         * Setting up ScanDone signal handler
         */
        scanDoneSignalHandler = new DBusSigHandler<NetworkManager.Device.Wireless.ScanDone>() {
            @Override
            public void handle(NetworkManager.Device.Wireless.ScanDone scanDone) {
                handleScanDoneDbusSignal(scanDone);
            }
        };

        dbusConnection.addSigHandler(NetworkManager.Device.Wireless.ScanDone.class, nwmProvider.getNWMDbusBusName(),
                nwmDeviceWirelessProperties, scanDoneSignalHandler);

        log.debug("Added deviceWireless sigHandler.ScanDone");

    }

    @Override
    public void shutdown() throws DBusException {
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Device.Wireless.PropertiesChanged.class,
                propertiesChangedSignalHandler);
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Device.Wireless.AccessPointAdded.class,
                accessPointAddedSignalHandler);
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Device.Wireless.AccessPointRemoved.class,
                accessPointRemovedSignalHandler);
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Device.Wireless.ScanDone.class,
                scanDoneSignalHandler);
    }

    /**
     * Handle a DBus signal for properties changes on the device.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handlePropertiesChangedDbusSignal(
            final NetworkManager.Device.Wireless.PropertiesChanged propertiesChanged) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("Properties changed for DeviceWireless " + propertiesChanged);
            }
        };
        new Thread(run).start();
    }

    /**
     * Handle a DBus signal for Access Point added.
     * 
     * @param accessPointAdded
     *            the DBus AccessPointAdded signal
     */
    private void handleAccessPointAddedDbusSignal(
            final NetworkManager.Device.Wireless.AccessPointAdded accessPointAdded) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("AccessPoint added for DeviceWireless " + accessPointAdded);
            }
        };
        new Thread(run).start();
    }

    /**
     * Handle a DBus signal for Access Point removed.
     * 
     * @param accessPointRemoved
     *            the DBus AccessPointRemoved signal
     */
    private void handleAccessPointRemovedDbusSignal(
            final NetworkManager.Device.Wireless.AccessPointRemoved accessPointRemoved) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("AccessPoint removed for DeviceWireless " + accessPointRemoved);
            }
        };
        new Thread(run).start();
    }

    /**
     * Handle a DBus signal for ScanDone.
     * 
     * @param scanDone
     *            the DBus Properties Changed signal
     */
    private void handleScanDoneDbusSignal(final NetworkManager.Device.Wireless.ScanDone scanDone) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("ScanDone for DeviceWireless " + scanDone);
            }
        };
        new Thread(run).start();
    }

    private Properties getProperties(String objectPath) throws DBusException {
        return nwmProvider.getDbusConnection().getRemoteObject(DBUS_NETWORKMANAGER, objectPath, Properties.class);
    }

    @Override
    public String getDeviceWirelessObjectPath() {
        return deviceWirelessObjectPath;
    }

    private NetworkManager.Settings getSettings(String objectPath) throws DBusException {
        return nwmProvider.getDbusConnection().getRemoteObject(DBUS_NETWORKMANAGER, objectPath,
                NetworkManager.Settings.class);
    }

    @Override
    public NetworkManager.Device.Wireless getDeviceWireless() {
        return nwmDeviceWireless;
    }

    @Override
    public NetworkManager.Settings getNWMSettings() {
        return nwmDeviceWirelessSettings;
    }

    @Override
    public Properties getNWMProperties() {
        return nwmDeviceWirelessProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#getHwAddress()
     */
    @Override
    public String getHwAddress() {
        Variant<String> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "HwAddress");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#
     * getPermHwAddress()
     */
    @Override
    public String getPermHwAddress() {
        Variant<String> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "PermHwAddress");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#getMode()
     */
    @Override
    public UInt32 getMode() {
        Variant<UInt32> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "Mode");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#getBitrate()
     */
    @Override
    public UInt32 getBitrate() {
        Variant<UInt32> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "Bitrate");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#getAccessPoints
     * ()
     */
    @Override
    public List<DBusInterface> getAllAccessPoints() {
        return nwmDeviceWireless.GetAllAccessPoints();
    }

    @Override
    public ZmartAccessPoint getAP(String objectPath) {
        try {
            return new ZmartAccessPoint(objectPath, getProperties(objectPath).GetAll(NWM_ACCESSPOINT_INTERFACE));
        } catch (DBusException e) {
            log.error("Error getting AP (AccessPoint)");
            return null;
        }

    }

    /*
     * Loop through devices and set up the wireless Device
     */
    @Override
    public List<ZmartAccessPoint> getAPs() {
        List<ZmartAccessPoint> aps = new ArrayList<ZmartAccessPoint>();
        getAllAccessPoints().forEach(ap -> {
            aps.add(getAP(ap.getObjectPath()));
        });
        return aps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#
     * getActiveAccessPoint()
     */
    @Override
    public Path getActiveAccessPoint() {
        Variant<Path> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "ActiveAccessPoint");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#
     * getWirelessCapabilities()
     */
    @Override
    public UInt32 getWirelessCapabilities() {
        Variant<UInt32> value = nwmDeviceWirelessProperties.Get(NWM_DEVICEWIRELESS_INTERFACE, "WirelessCapabilities");
        return value.getValue();
    }

    @Override
    public List<NMDeviceWifiCapabilities> getWirelessCapabilitiesEnums() {
        List<NMDeviceWifiCapabilities> ret = new ArrayList<NMDeviceWifiCapabilities>();
        UInt32 value = getWirelessCapabilities();
        for (NMDeviceWifiCapabilities wifiCap : NMDeviceWifiCapabilities.values()) {
            if (wifiCap.isSet(value)) {
                ret.add(wifiCap);
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDeviceWireless#getAccessPoints
     * ()
     */
    @Override
    public List<Path> getAccessPoints() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getAvailableConnections
     * ()
     */

}
