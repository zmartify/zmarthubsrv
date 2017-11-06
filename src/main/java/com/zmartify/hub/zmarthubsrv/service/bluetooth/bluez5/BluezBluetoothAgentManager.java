/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5;

import org.bluez.Agent1;
import org.bluez.AgentManager1;

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

import org.freedesktop.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAgent;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAgentManager;

/**
 * A Bluetooth Adapter using the BlueZ provider.
 * 
 * @author Keith M. Hughes
 */
public class BluezBluetoothAgentManager implements IBluezBluetoothAgentManager {

    private static final Logger log = LoggerFactory.getLogger(BluezBluetoothAgentManager.class);

    /**
     * The Bluez remote object for the bluetooth agentManager.
     */
    private AgentManager1 bluezAgentManager;

    /**
     * The DBus properties remote object for the agentManager.
     */
    private Properties bluezAgentManagerProperties;

    /**
     * The DBus object path for the agentManager.
     */
    private String agentManagerObjectPath;

    private Agent1 bluezAgent = null;

    /**
     * The Bluez provider that this agentManager is running under.
     */
    private BluezBluetoothProvider bluezProvider;

    /**
     * A DBus signal handler for handling property changed signals.
     */
    private DBusSigHandler<Properties.PropertiesChanged> propertiesChangedSignalHandler;

    /**
     * Construct a new agentManager.
     * 
     * @param bluezProvider
     *            the BlueZ provider for DBus
     * @param agentManagerObjectPath
     *            the DBus object path for the agentManager
     */
    public BluezBluetoothAgentManager(BluezBluetoothProvider bluezProvider, String agentManagerObjectPath) {
        this.bluezProvider = bluezProvider;
        this.agentManagerObjectPath = agentManagerObjectPath;
    }

    @Override
    public void startup() throws Exception {
        DBusConnection dbusConnection = bluezProvider.getDbusConnection();

        bluezAgentManager = (AgentManager1) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME,
                agentManagerObjectPath, AgentManager1.class);

        bluezAgentManagerProperties = (Properties) dbusConnection.getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME,
                agentManagerObjectPath, Properties.class);

        log.info("Properties AgentManager: \n {}", bluezAgentManagerProperties);

        propertiesChangedSignalHandler = new DBusSigHandler<Properties.PropertiesChanged>() {
            @Override
            public void handle(Properties.PropertiesChanged propertiesChanged) {
                handlePropertiesChangedDbusSignal(propertiesChanged);
            }
        };

        dbusConnection.addSigHandler(Properties.PropertiesChanged.class, bluezProvider.getBluezDbusBusName(),
                bluezAgentManagerProperties, propertiesChangedSignalHandler);

    }

    @Override
    public void shutdown() throws Exception {
        // If an Agent has been registered, then unregister it
        if (bluezAgent != null) {
            bluezAgentManager.UnregisterAgent(bluezAgent);
            bluezAgent = null;
            log.info("Agent unregistered.");
        }

        bluezProvider.getDbusConnection().removeSigHandler(Properties.PropertiesChanged.class,
                propertiesChangedSignalHandler);
    }

    @Override
    public IBluezBluetoothAgent newAgent(IBluezBluetoothAgent agent) {

        // Export it to DBus
        try {
            bluezProvider.getDbusConnection().exportObject(agent.getObjectPath(), agent);
        } catch (DBusException e) {
            log.error("Error exporting bluezAgent {} :: {}", agent.getObjectPath(), e.getMessage());
            return null;
        }

        // Get the agent reference on DBus
        try {
            bluezAgent = (Agent1) bluezProvider.getDbusConnection().getRemoteObject(BluezConstants.BLUEZ_DBUS_BUSNAME,
                    agent.getObjectPath(), Agent1.class);
        } catch (DBusException e) {
            log.error("Error getting bluezAgent {} :: {}", agent.getObjectPath(), e.getMessage());
            return null;
        }

        bluezAgentManager.RegisterAgent(bluezAgent, "NoInputNoOutput");
        bluezAgentManager.RequestDefaultAgent(bluezAgent);

        log.info("Agent registered.");
        return agent;
    }

    /**
     * Handle a DBus signal for properties changes on the agentManager.
     * 
     * @param propertiesChanged
     *            the DBus Properties Changed signal
     */
    private void handlePropertiesChangedDbusSignal(Properties.PropertiesChanged propertiesChanged) {
        log.debug("Properties changed " + propertiesChanged.getPropertiesChanged());
    }

    void getProperties() {
        log.debug("Properties: {}", bluezAgentManagerProperties.GetAll(BluezConstants.BLUEZ_AGENTMANAGER_INTERFACE));
    }
}