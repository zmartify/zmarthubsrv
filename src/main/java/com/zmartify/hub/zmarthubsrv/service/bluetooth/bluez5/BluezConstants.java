/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5;

/**
 * A collection of constants for working with the BlueZ DBus API.
 * 
 * @author Keith M. Hughes
 */
public class BluezConstants {

    /**
     * The well-known DBus bus name for BlueZ
     */
    public static final String BLUEZ_DBUS_BUSNAME = "org.bluez";

    /**
     * The Bluez DBus interface for Bluetooth adapters.
     */
    public static final String BLUEZ_ADAPTER_INTERFACE = "org.bluez.Adapter1";
    public static final String BLUEZ_ADAPTER_PATH = "/org/bluez/hci0";

    /**
     * The Bluez DBus interface for Bluetooth adapters.
     */
    public static final String BLUEZ_DEVICE_INTERFACE = "org.bluez.Device1";

    /**
     * The Bluez DBus interface for Bluetooth agents.
     */
    public static final String BLUEZ_AGENT_INTERFACE = "org.bluez.Agent1";
    public static final String BLUEZ_AGENT_PATH = "/org/bluez/Agent1";

    /**
     * The Bluez DBus interface for Bluetooth agentManagers.
     */
    public static final String BLUEZ_AGENTMANAGER_INTERFACE = "org.bluez.AgentManager1";
    public static final String BLUEZ_AGENTMANAGER_PATH = "/org/bluez";

}