/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.util.List;
import java.util.Map;

import org.freedesktop.NetworkManager;
import org.freedesktop.NetworkManager.Device;
import org.freedesktop.Pair;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Kristensen
 *
 */
public class NWMDevice implements INWMDevice {

    private static final Logger log = LoggerFactory.getLogger(NWMDevice.class);

    private NetworkManager.Device nwmDevice;

    private Properties nwmDeviceProperties;

    private String deviceObjectPath;

    private INWMProvider nwmProvider;

    /**
     * The signal handler for changing properties.
     */
    private DBusSigHandler<NetworkManager.Device.StateChanged> stateChangedSignalHandler;

    /**
     * Construct a new AccessPoint
     * 
     * @param nwmProvider
     * @param accessPointObjectPath
     */
    public NWMDevice(INWMProvider nwmProvider, String accessPointObjectPath) {
        this.nwmProvider = nwmProvider;
        this.deviceObjectPath = accessPointObjectPath;
    }

    @Override
    public void startup() throws Exception {
        DBusConnection dbusConnection = nwmProvider.getDbusConnection();

        nwmDevice = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, deviceObjectPath, Device.class);

        log.debug("Got device '{}'", nwmDevice);

        nwmDeviceProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, deviceObjectPath, Properties.class);

        log.debug("Got device.Properties '{}'", nwmDeviceProperties);

        stateChangedSignalHandler = new DBusSigHandler<NetworkManager.Device.StateChanged>() {
            @Override
            public void handle(NetworkManager.Device.StateChanged stateChanged) {
                handleStateChangedDbusSignal(stateChanged);
            }
        };

        dbusConnection.addSigHandler(NetworkManager.Device.StateChanged.class, nwmProvider.getNWMDbusBusName(),
                nwmDeviceProperties, stateChangedSignalHandler);

        log.debug("Added Device sigHandler.StateChanged ");
    }

    @Override
    public void shutdown() throws Exception {
        nwmProvider.getDbusConnection().removeSigHandler(NetworkManager.Device.StateChanged.class,
                stateChangedSignalHandler);
    }

    /**
     * Handle a DBus signal for properties changes on the device.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handleStateChangedDbusSignal(final NetworkManager.Device.StateChanged stateChanged) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                log.info("State changed for Device ", stateChanged);
            }
        };
        new Thread(run).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getSsid()
     */
    @Override
    public Map<String, Variant<?>> getLldpNeighbors() {
        Variant<Map<String, Variant<?>>> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "LldpNeighbors");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getAvailableConnections
     * ()
     */
    @Override
    public List<DBusInterface> getAvailableConnections() {
        Variant<List<DBusInterface>> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "AvailableConnections");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getAutoconnect()
     */
    @Override
    public boolean getAutoconnect() {
        Variant<Boolean> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Autoconnect");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getFirmwareMissing()
     */
    @Override
    public boolean getFirmwareMissing() {
        Variant<Boolean> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "FirmwareMissing");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getManaged()
     */
    @Override
    public boolean getManaged() {
        Variant<Boolean> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Managed");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getNmPluginMissing()
     */
    @Override
    public boolean getNmPluginMissing() {
        Variant<Boolean> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "NmPluginMissing");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#Real()
     */
    @Override
    public boolean Real() {
        Variant<Boolean> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Real");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getActiveConnection()
     */
    @Override
    public DBusInterface getActiveConnection() {
        Variant<DBusInterface> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "ActiveConnectin");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getDhcp4Config()
     */
    @Override
    public DBusInterface getDhcp4Config() {
        Variant<DBusInterface> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Dhcp4Config");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getDhcp6Config()
     */
    @Override
    public DBusInterface getDhcp6Config() {
        Variant<DBusInterface> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Dhcp6Config");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getIp4Config()
     */
    @Override
    public DBusInterface getIp4Config() {
        Variant<DBusInterface> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Ip4Config");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getIp6Config()
     */
    @Override
    public DBusInterface getIp6Config() {
        Variant<DBusInterface> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Ip6Config");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getDriver()
     */
    @Override
    public String getDriver() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Driver");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getDriverVersion()
     */
    @Override
    public String getDriverVersion() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "DriverVersion");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getFirmwareVersion()
     */
    @Override
    public String getFirmwareVersion() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "FirmwareVersion");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getInterface()
     */
    @Override
    public String getInterface() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Interface");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getPhysicalPort()
     */
    @Override
    public String getPhysicalPort() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "PhysicalPort");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getUdi()
     */
    @Override
    public String getUdi() {
        Variant<String> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Udi");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getStateReason()
     */
    @Override
    public Pair<UInt32, UInt32> getStateReason() {
        Variant<Pair<UInt32, UInt32>> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "StateReason");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getCapabilities()
     */
    @Override
    public UInt32 getCapabilities() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Capabilities");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getDeviceType()
     */
    @Override
    public UInt32 getDeviceType() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "DeviceType");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getIp4Address()
     */
    @Override
    public UInt32 getIp4Address() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Ip4Address");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getMetered()
     */
    @Override
    public UInt32 getMetered() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Metered");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getMtu()
     */
    @Override
    public UInt32 getMtu() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "Mtu");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getState()
     */
    @Override
    public UInt32 getState() {
        Variant<UInt32> value = nwmDeviceProperties.Get(NWM_DEVICE_INTERFACE, "State");
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMDevice#getState()
     */
    @Override
    public Map<String, Variant<?>> getAll() {
        return nwmDeviceProperties.GetAll(NWM_DEVICE_INTERFACE);
    }

}
