/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5;

import java.util.List;

/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.bluez.Device1;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothDevice;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothProvider;

/**
 * A Bluetooth device accessed through Bluez.
 * 
 * @author Keith M. Hughes
 */
public class BluezBluetoothDevice implements IBluezBluetoothDevice {

    private static final Logger log = LoggerFactory.getLogger(BluezBluetoothDevice.class);

    private static final String BLUEZ_PROPERTY_DEVICE_ADDRESS = "Address";
    private static final String BLUEZ_PROPERTY_DEVICE_NAME = "Name";
    private static final String BLUEZ_PROPERTY_DEVICE_ALIAS = "Alias";
    // private static final String BLUEZ_PROPERTY_DEVICE_CLASS = "Class";
    private static final String BLUEZ_PROPERTY_DEVICE_APPEREANCE = "Appereance";
    private static final String BLUEZ_PROPERTY_DEVICE_ICON = "Icon";
    // private static final String BLUEZ_PROPERTY_DEVICE_PAIRED = "Paired";
    private static final String BLUEZ_PROPERTY_DEVICE_TRUSTED = "Trusted";
    private static final String BLUEZ_PROPERTY_DEVICE_BLOCKED = "Blocked";
    // private static final String BLUEZ_PROPERTY_DEVICE_LEGACYPAIRING = "LegacyPairing";
    private static final String BLUEZ_PROPERTY_DEVICE_RSSI = "RSSI";
    private static final String BLUEZ_PROPERTY_DEVICE_CONNECTED = "Connected";
    private static final String BLUEZ_PROPERTY_DEVICE_UUIDS = "UUIDS";
    // private static final String BLUEZ_PROPERTY_DEVICE_MODALIAS = "Modalias";
    // private static final String BLUEZ_PROPERTY_DEVICE_ADAPTER = "Adapter";

    /**
     * The Bluez remote object for the bluetooth device.
     */
    private Device1 bluezDevice;

    /**
     * The DBus properties interface for the bluetooth device.
     */
    private Properties bluezDeviceProperties;

    /**
     * The DBus object path for the device.
     */
    private String deviceObjectPath;

    /**
     * The bluez provider.
     */
    private IBluezBluetoothProvider bluezProvider;

    /**
     * The signal handler for changing properties.
     */
    private DBusSigHandler<Properties.PropertiesChanged> propertiesChangedSignalHandler;

    /**
     * Construct a new bluetooth device.
     * 
     * @param bluezProvider2
     *            the bluez provider
     * @param deviceObjectPath
     *            the DBus object path to the device
     */
    public BluezBluetoothDevice(IBluezBluetoothProvider bluezProvider, String deviceObjectPath) {
        this.bluezProvider = bluezProvider;
        this.deviceObjectPath = deviceObjectPath;
    }

    @Override
    public void startup() throws Exception {
        DBusConnection dbusConnection = bluezProvider.getDbusConnection();

        bluezDevice = (Device1) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME, deviceObjectPath,
                Device1.class);

        System.out.println("Got bluez device " + bluezDevice);

        bluezDeviceProperties = (Properties) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME,
                deviceObjectPath, Properties.class);

        propertiesChangedSignalHandler = new DBusSigHandler<Properties.PropertiesChanged>() {
            @Override
            public void handle(Properties.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        dbusConnection.addSigHandler(Properties.PropertiesChanged.class, bluezProvider.getBluezDbusBusName(),
                bluezDeviceProperties, propertiesChangedSignalHandler);
    }

    @Override
    public void shutdown() throws Exception {
        bluezProvider.getDbusConnection().removeSigHandler(Properties.PropertiesChanged.class,
                propertiesChangedSignalHandler);
    }

    void getProperties() {
        log.info("DeviceProperties: {}", bluezDeviceProperties.GetAll("org.bluez.Device1"));
    }

    @Override
    public void connect() {
        bluezDevice.Connect();
    }

    @Override
    public void disconnect() {
        bluezDevice.Disconnect();
    }

    @Override
    public String getId() {
        Variant<String> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_ADDRESS);

        return value.getValue();
    }

    @Override
    public String getName() {
        Variant<String> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_NAME);

        return value.getValue();
    }

    @Override
    public int getRssi() {
        Variant<Short> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_RSSI);

        return value.getValue();
    }

    @Override
    public boolean isConnected() {
        Variant<Boolean> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_CONNECTED);

        return value.getValue();
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
                System.out
                        .println("Properties changed for Bluetooth device " + propertiesChanged.getPropertiesChanged());
            }
        };

        new Thread(run).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#getAlias()
     */
    @Override
    public String getAlias() {
        Variant<String> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_ALIAS);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#setAlias(
     * java.lang.String)
     */
    @Override
    public void setAlias(String alias) {
        bluezDeviceProperties.Set(BluezConstants.BLUEZ_DEVICE_INTERFACE, BLUEZ_PROPERTY_DEVICE_ALIAS,
                new Variant<String>(alias));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#
     * getApperance()
     */
    @Override
    public String getApperance() {
        Variant<String> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_APPEREANCE);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#getIcon()
     */
    @Override
    public String getIcon() {
        Variant<String> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_ICON);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#getTrusted(
     * )
     */
    @Override
    public boolean getTrusted() {
        Variant<Boolean> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_TRUSTED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#setTrusted(
     * boolean)
     */
    @Override
    public void setTrusted(boolean trusted) {
        bluezDeviceProperties.Set(BluezConstants.BLUEZ_DEVICE_INTERFACE, BLUEZ_PROPERTY_DEVICE_TRUSTED,
                new Variant<Boolean>(trusted));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#getBlocked(
     * )
     */
    @Override
    public boolean getBlocked() {
        Variant<Boolean> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_BLOCKED);
        return value.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#setBlocked(
     * boolean)
     */
    @Override
    public void setBlocked(boolean blocked) {
        bluezDeviceProperties.Set(BluezConstants.BLUEZ_DEVICE_INTERFACE, BLUEZ_PROPERTY_DEVICE_BLOCKED,
                new Variant<Boolean>(blocked));

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothDevice#getUUIDs()
     */
    @Override
    public List<String> getUUIDs() {
        Variant<List<String>> value = bluezDeviceProperties.Get(BluezConstants.BLUEZ_DEVICE_INTERFACE,
                BLUEZ_PROPERTY_DEVICE_UUIDS);
        return value.getValue();
    }
}