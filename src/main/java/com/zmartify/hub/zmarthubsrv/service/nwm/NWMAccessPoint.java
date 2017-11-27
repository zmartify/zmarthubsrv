/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.util.Map;

import org.freedesktop.NetworkManager;
import org.freedesktop.NetworkManager.AccessPoint;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Kristensen
 *
 */
public class NWMAccessPoint implements INWMAccessPoint {

    private static final Logger log = LoggerFactory.getLogger(NWMAccessPoint.class);

    private NetworkManager.AccessPoint nwmAccessPoint;

    private Properties nwmAccessPointProperties;

    private NetworkManager.Settings nwmAccessPointSettings;

    private String accessPointObjectPath;

    private INWMProvider nwmProvider;

    /**
     * The signal handler for changing properties.
     */
    private DBusSigHandler<Properties.PropertiesChanged> propertiesChangedSignalHandler;

    /**
     * Construct a new AccessPoint
     * 
     * @param nwmProvider
     * @param accessPointObjectPath
     */
    public NWMAccessPoint(INWMProvider nwmProvider, String accessPointObjectPath) {
        this.nwmProvider = nwmProvider;
        this.accessPointObjectPath = accessPointObjectPath;

        DBusConnection dbusConnection = this.nwmProvider.getDbusConnection();

        try {

            nwmAccessPoint = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, accessPointObjectPath,
                    AccessPoint.class);
            log.debug("Got accessPoint '{}'", nwmAccessPoint);

            nwmAccessPointProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, accessPointObjectPath,
                    Properties.class);
            log.debug("Got accessPoint.Properties '{}'", nwmAccessPointProperties);

            nwmAccessPointSettings = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, accessPointObjectPath,
                    NetworkManager.Settings.class);

            log.debug("Got accessPoint.Settings '{}'", nwmAccessPointSettings);

        } catch (DBusException e) {
            log.error("Error constructing AccessPoint");
        }
    }

    @Override
    public void startup() throws Exception {

        addChangedSignalHandler();

    }

    @Override
    public void shutdown() throws Exception {
        nwmProvider.getDbusConnection().removeSigHandler(Properties.PropertiesChanged.class,
                propertiesChangedSignalHandler);
    }

    private void addChangedSignalHandler() throws DBusException {
        propertiesChangedSignalHandler = new DBusSigHandler<Properties.PropertiesChanged>() {
            @Override
            public void handle(Properties.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        nwmProvider.getDbusConnection().addSigHandler(Properties.PropertiesChanged.class,
                nwmProvider.getNWMDbusBusName(), nwmAccessPointProperties, propertiesChangedSignalHandler);

        log.debug("Added accessPoint sigHandler.PropertiesChanged ");
    }
    /**
     * Handle a DBus signal for properties changes on the device.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handlePropertiesChangedDbusSignal(final Properties.PropertiesChanged propertiesChanged) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.debug("Properties changed for AccessPoint " + propertiesChanged.getPropertiesChanged());
            }
        };
        new Thread(run).start();
    }

    @Override
    public String getAccessPointObjectPath() {
        return accessPointObjectPath;
    }

    @Override
    public NetworkManager.Settings getSettings() {
        return nwmAccessPointSettings;
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getSsid()
     */

    @Override
    public NetworkManager.AccessPoint getAccessPoint() {
        return nwmAccessPoint;
    }

    @Override
    public byte[] getSsid() {

        Variant<byte[]> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_SSID);
        return value.getValue();
    }

    public String getSsidAsString() {
        return new String(getSsid());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getStrength()
     */
    @Override
    public byte getStrength() {
        Variant<Byte> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_STRENGTH);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getLastSeen()
     */
    @Override
    public short getLastSeen() {
        Variant<Short> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_LASTSEEN);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getHwAddress()
     */
    @Override
    public String getHwAddress() {
        Variant<String> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_HWADDRESS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getFlags()
     */
    @Override
    public UInt32 getFlags() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_FLAGS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getFrequency()
     */
    @Override
    public UInt32 getFrequency() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_FREQUENCY);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getMaxBitRate()
     */
    @Override
    public UInt32 getMaxBitRate() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_MAXBITRATE);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getMode()
     */
    @Override
    public UInt32 getMode() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_MODE);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getRsnFlags()
     */
    @Override
    public UInt32 getRsnFlags() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_RSNFLAGS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getWpaFlags()
     */
    @Override
    public UInt32 getWpaFlags() {
        Variant<UInt32> value = nwmAccessPointProperties.Get(NWM_ACCESSPOINT_INTERFACE, NWM_ACCESSPOINT_WPAFLAGS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getAll()
     */
    @Override
    public Map<String, Variant<?>> getAll() {
        return nwmAccessPointProperties.GetAll(NWM_ACCESSPOINT_INTERFACE);
    }

}
