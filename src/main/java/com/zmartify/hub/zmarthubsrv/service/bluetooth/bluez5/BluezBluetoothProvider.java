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

import static com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5.BluezConstants.*;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freedesktop.DBus;
import org.freedesktop.ObjectManager;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAdapter;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAgent;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAgentManager;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothProvider;

/**
 * The provider for acess to the BlueZ bluetooth services in DBus.
 * 
 * @author Keith M. Hughes
 */
public class BluezBluetoothProvider implements IBluezBluetoothProvider {

    private static final Logger log = LoggerFactory.getLogger(BluezBluetoothProvider.class);

    private IBluezBluetoothAdapter bluezAdapter = null;

    private IBluezBluetoothAgent bluezAgent = null;

    private IBluezBluetoothAgentManager bluezAgentManager = null;

    /**
     * The DBus connection used to talk to the Bluez service.
     */
    private DBusConnection dbusConnection = null;

    /**
     * The DBus ObjectManager for the root Bluez object.
     */
    private ObjectManager bluezObjectManager;

    /**
     * The DBus signal handler for the ObjectManager's InterfacesAdded signal.
     */
    private DBusSigHandler<ObjectManager.InterfacesAdded> interfacesAddedSignalHandler;

    /**
     * The DBus signal handler for the ObjectManager's InterfacesRemoved signal.
     */
    private DBusSigHandler<ObjectManager.InterfacesRemoved> interfacesRemovedSignalHandler;

    /**
     * The unique name of the DBus bus for Bluez.
     */
    private String bluezDbusBusName;

    public BluezBluetoothProvider() {

    }

    @Override
    public void startup() throws Exception {

        dbusConnection = DBusConnection.getConnection(DBusConnection.SYSTEM);

        DBus dbus = dbusConnection.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);

        bluezDbusBusName = dbus.GetNameOwner("org.bluez");

        bluezObjectManager = (ObjectManager) dbusConnection.getRemoteObject("org.bluez", "/", ObjectManager.class);

        interfacesAddedSignalHandler = new DBusSigHandler<ObjectManager.InterfacesAdded>() {
            @Override
            public void handle(ObjectManager.InterfacesAdded signal) {
                log.info("Interfaces added {} :: {}", signal.getObjectPath(), signal.getName());
            }
        };

        interfacesRemovedSignalHandler = new DBusSigHandler<ObjectManager.InterfacesRemoved>() {

            @Override
            public void handle(ObjectManager.InterfacesRemoved signal) {
                log.info("Interfaces removed");
            }
        };

        dbusConnection.addSigHandler(ObjectManager.InterfacesAdded.class, bluezDbusBusName, bluezObjectManager,
                interfacesAddedSignalHandler);

        dbusConnection.addSigHandler(ObjectManager.InterfacesRemoved.class, bluezDbusBusName, bluezObjectManager,
                interfacesRemovedSignalHandler);

        bluezAdapter = newAdapter(BLUEZ_ADAPTER_PATH);
        bluezAdapter.startup();

        bluezAgentManager = newAgentManager(BLUEZ_AGENTMANAGER_PATH);
        bluezAgentManager.startup();

        bluezAgent = bluezAgentManager.newAgent(new BluezBluetoothAgent(this, BLUEZ_AGENT_PATH));
    }

    @Override
    public void shutdown() throws Exception {
        dbusConnection.removeSigHandler(ObjectManager.InterfacesAdded.class, interfacesAddedSignalHandler);
        dbusConnection.removeSigHandler(ObjectManager.InterfacesRemoved.class, interfacesRemovedSignalHandler);

        dbusConnection.disconnect();
    }

    public IBluezBluetoothAdapter getBluezAdapter() {
        return bluezAdapter;
    }

    public IBluezBluetoothAgent getBluezAgent() {
        return bluezAgent;
    }

    public IBluezBluetoothAgentManager getBluezAgentManager() {
        return bluezAgentManager;
    }

    public ObjectManager getBluezObjectManager() {
        return bluezObjectManager;
    }

    @Override
    public List<String> listAdapters() throws Exception {
        return getObjectsByInterface(BluezConstants.BLUEZ_ADAPTER_INTERFACE);
    }

    @Override
    public List<String> listDevices() throws Exception {
        return getObjectsByInterface(BluezConstants.BLUEZ_DEVICE_INTERFACE);
    }

    public List<String> listAgents() throws Exception {
        return getObjectsByInterface(BluezConstants.BLUEZ_AGENT_INTERFACE);
    }

    @Override
    public IBluezBluetoothAdapter newAdapter(String adapterObjectPath) {
        return new BluezBluetoothAdapter(this, adapterObjectPath);
    }

    public IBluezBluetoothAgentManager newAgentManager(String agentManagerObjectPath) {
        return new BluezBluetoothAgentManager(this, agentManagerObjectPath);
    }

    /**
     * Get a list of all BlueZ objects that implement a given DBus interface.
     * 
     * @param objectInterface
     *            the name of the interface to scan for
     * 
     * @return a list of all objects implementing the specified interface
     */
    private List<String> getObjectsByInterface(String objectInterface) {
        List<String> results = new ArrayList<>();

        for (Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> objectByPath : bluezObjectManager
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

    /**
     * Get the unique DBus bus name for the BlueZ service.
     * 
     * @return the BlueZ DBus bus name
     */
    public String getBluezDbusBusName() {
        return bluezDbusBusName;
    }

}