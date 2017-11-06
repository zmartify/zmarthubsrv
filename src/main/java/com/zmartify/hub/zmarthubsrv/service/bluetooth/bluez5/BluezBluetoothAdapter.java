/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5;

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

import org.bluez.Adapter1;
import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;

import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAdapter;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothDevice;

import java.util.List;

/**
 * A Bluetooth Adapter using the BlueZ provider.
 * 
 * @author Keith M. Hughes
 */
public class BluezBluetoothAdapter implements IBluezBluetoothAdapter {

    /**
     * The Bluez remote object for the bluetooth adapter.
     */
    private Adapter1 bluezAdapter;

    /**
     * The DBus properties remote object for the adapter.
     */
    private Properties bluezAdapterProperties;

    /**
     * The DBus object path for the adapter.
     */
    private String adapterObjectPath;

    /**
     * The Bluez provider that this adapter is running under.
     */
    private BluezBluetoothProvider bluezProvider;

    /**
     * A DBus signal handler for handling property changed signals.
     */
    private DBusSigHandler<Properties.PropertiesChanged> propertiesChangedSignalHandler;

    /**
     * Construct a new adapter.
     * 
     * @param bluezProvider
     *            the BlueZ provider for DBus
     * @param adapterObjectPath
     *            the DBus object path for the adapter
     */
    public BluezBluetoothAdapter(BluezBluetoothProvider bluezProvider, String adapterObjectPath) {
        this.bluezProvider = bluezProvider;
        this.adapterObjectPath = adapterObjectPath;
    }

    @Override
    public void startup() throws Exception {
        DBusConnection dbusConnection = bluezProvider.getDbusConnection();

        bluezAdapter = (Adapter1) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME, adapterObjectPath,
                Adapter1.class);

        bluezAdapterProperties = (Properties) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME,
                adapterObjectPath, Properties.class);

        propertiesChangedSignalHandler = new DBusSigHandler<Properties.PropertiesChanged>() {
            @Override
            public void handle(Properties.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        dbusConnection.addSigHandler(Properties.PropertiesChanged.class, bluezProvider.getBluezDbusBusName(),
                bluezAdapterProperties, propertiesChangedSignalHandler);

        // Operating systems will sometimes cache device information. This will
        // make sure that everything is
        // cleaned out. If a device is already there, it will detected by BlueZ
        // quickly.
        List<String> devices = bluezProvider.listDevices();
        for (String devicePath : devices) {
            if (devicePath.startsWith(adapterObjectPath)) {
                bluezAdapter.RemoveDevice(new Path(devicePath));
            }
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (isScanning()) {
            bluezAdapter.StopDiscovery();
        }

        bluezProvider.getDbusConnection().removeSigHandler(Properties.PropertiesChanged.class,
                propertiesChangedSignalHandler);
    }

    @Override
    public void startScanning() throws Exception {
        bluezAdapter.StartDiscovery();
    }

    @Override
    public void stopScanning() throws Exception {
        bluezAdapter.StopDiscovery();
    }

    @Override
    public boolean isScanning() {
        Variant<Boolean> scanning = bluezAdapterProperties.Get(BluezConstants.BLUEZ_ADAPTER_INTERFACE, "Discovering");

        return scanning.getValue();
    }

    @Override
    public void setPowered(boolean powered) {
        bluezAdapterProperties.Set(BluezConstants.BLUEZ_ADAPTER_INTERFACE, "Powered", new Variant<Boolean>(powered));
    }

    @Override
    public boolean isPowered() {
        Variant<Boolean> powered = bluezAdapterProperties.Get(BluezConstants.BLUEZ_ADAPTER_INTERFACE, "Powered");

        return powered.getValue();
    }

    @Override
    public IBluezBluetoothDevice newDevice(String address) {
        return new BluezBluetoothDevice(bluezProvider, adapterObjectPath + "/dev_" + address.replace(':', '_'));
    }

    /**
     * Handle a DBus signal for properties changes on the adapter.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handlePropertiesChangedDbusSignal(Properties.PropertiesChanged propertiesChanged) {
        System.out.println("Properties changed " + propertiesChanged.getPropertiesChanged());
    }

    void getProperties() {
        System.out.println(bluezAdapterProperties.GetAll(BluezConstants.BLUEZ_ADAPTER_INTERFACE));
    }
}