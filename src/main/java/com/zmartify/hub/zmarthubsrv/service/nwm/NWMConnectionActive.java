/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import static com.zmartify.hub.zmarthubsrv.service.nwm.NWMConstants.*;

import java.util.List;
import java.util.Map;

import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Kristensen
 *
 */
public class NWMConnectionActive implements INWMConnectionActive {

    private static final Logger log = LoggerFactory.getLogger(NWMConnectionActive.class);

    private Properties nwmConnectionProperties;

    private INWMProvider nwmProvider;

    private String connectionObjectPath;

    /**
     * Construct a new AccessPoint
     * 
     * @param nwmProvider
     * @param accessPointObjectPath
     */
    public NWMConnectionActive(INWMProvider nwmProvider, String connectionObjectPath) {
        this.nwmProvider = nwmProvider;
        this.connectionObjectPath = connectionObjectPath;

        DBusConnection dbusConnection = this.nwmProvider.getDbusConnection();

        try {
            nwmConnectionProperties = dbusConnection.getRemoteObject(DBUS_NETWORKMANAGER, connectionObjectPath,
                    Properties.class);
    
        } catch (DBusException e) {
            log.error("Error constructing Connectin");
        }
    }

    @Override
    public String getConnectionObjectPath() {
        return connectionObjectPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.nwm.INWMAccessPoint#getAll()
     */
    @Override
    public Map<String, Variant<?>> getAll() {
        return nwmConnectionProperties.GetAll(NWM_CONNECTIONACTIVE_INTERFACE);
    }

	@Override
	public Path getSpecificObject() {
        Variant<Path> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_SPECIFICOBJECT);
		return value.getValue();
	}

	@Override
	public String getId() {
        Variant<String> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_ID);
		return value.getValue();
	}

	@Override
	public String getUuid() {
        Variant<String> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_UUID);
		return value.getValue();
	}

	@Override
	public String getType() {
        Variant<String> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_TYPE);
		return value.getValue();
	}

	@Override
	public List<Path> getDevices() {
        Variant<List<Path>> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_DEVICES);
		return value.getValue();
	}

	@Override
	public UInt32 getState() {
        Variant<UInt32> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_STATE);
		return value.getValue();
	}

	@Override
	public UInt32 getStatFlags() {
        Variant<UInt32> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_STATEFLAGS);
		return value.getValue();
	}

	@Override
	public boolean getDefault() {
        Variant<Boolean> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_DEFAULT);
		return value.getValue();
	}

	@Override
	public DBusInterface getIp4Config() {
        Variant<DBusInterface> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_IP4CONFIG);
		return value.getValue();
	}

	@Override
	public DBusInterface getDhcp4Config() {
        Variant<DBusInterface> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_DHCP4CONFIG);
		return value.getValue();
	}

	@Override
	public boolean getDefault6() {
        Variant<Boolean> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_DEFAULT6);
		return value.getValue();
	}

	@Override
	public DBusInterface getIp6Config() {
        Variant<DBusInterface> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_IP6CONFIG);
		return value.getValue();
	}

	@Override
	public DBusInterface getDhcp6Config() {
        Variant<DBusInterface> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_DHCP6CONFIG);
		return value.getValue();
	}

	@Override
	public boolean getVpn() {
        Variant<Boolean> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_VPN);
		return value.getValue();
	}

	@Override
	public DBusInterface getMaster() {
        Variant<DBusInterface> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_MASTER);
		return value.getValue();
	}

	@Override
	public Path getConnection() {
        Variant<Path> value = nwmConnectionProperties.Get(NWM_CONNECTIONACTIVE_INTERFACE,NWM_CONNECTIONACTIVE_CONNECTION);
		return value.getValue();
	}

}
