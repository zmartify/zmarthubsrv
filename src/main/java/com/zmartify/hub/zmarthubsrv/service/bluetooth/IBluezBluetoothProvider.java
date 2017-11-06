/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth;

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

import java.util.List;

import org.freedesktop.dbus.DBusConnection;

/**
 * A provider of Bluetooth functionality.
 * 
 * @author Keith M. Hughes
 */
public interface IBluezBluetoothProvider {

    void startup() throws Exception;

    void shutdown() throws Exception;

    /**
     * Get a list of all currently known Bluetooth adapters.
     * 
     * @return the names of all currently known adapters
     * 
     * @throws Exception
     */
    List<String> listAdapters() throws Exception;

    /**
     * Get a list of all currently known Bluetooth devices.
     * 
     * @return the names of all currently known devices
     * 
     * @throws Exception
     */
    List<String> listDevices() throws Exception;

    /**
     * Get a bluetooth adapter given its name.
     * 
     * @param adapterObjectPath
     * 
     * @return the adapter object
     */
    IBluezBluetoothAdapter newAdapter(String adapterObjectPath);

    /**
     * @return
     */
    DBusConnection getDbusConnection();

    /**
     * @return
     */
    String getBluezDbusBusName();

}