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

import org.bluez.Agent1;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothAgent;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothDevice;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothProvider;

/**
 * A Bluetooth agent accessed through Bluez.
 * 
 * @author Keith M. Hughes
 * @author Peter Kristensen
 * 
 */

public class BluezBluetoothAgent implements IBluezBluetoothAgent {

    private static final Logger log = LoggerFactory.getLogger(Agent1.class);

    /**
     * The pinCode to be used for pairing
     */
    private String bluezPinCode = "1234";

    private String bluezAgentObjectPath;

    private IBluezBluetoothProvider bluezProvider;

    /**
     * Construct a new bluetooth agent.
     * 
     * @param bluezProvider
     *            the bluez provider
     * @param agentObjectPath
     *            the DBus object path to the agent
     */
    public BluezBluetoothAgent(IBluezBluetoothProvider bluezProvider, String bluezAgentObjectPath) {
        this.bluezProvider = bluezProvider;
        this.bluezAgentObjectPath = bluezAgentObjectPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.service.bluetooth.IBluetoothAgent#getPinCode()
     */
    @Override
    public String getPinCode() {
        return bluezPinCode;
    }

    public void setPinCode(String bluezPinCode) {
        this.bluezPinCode = bluezPinCode;
    }

    @Override
    public void Release() {
    }

    @Override
    public String RequestPinCode(Path path) {
        return bluezPinCode;
    }

    /**
     * This method gets called when the service daemon needs to display a pincode
     * for an authentication. An empty reply should be returned. When the pincode
     * needs no longer to be displayed, the Cancel method of the agent will be
     * called. This is used during the pairing process of keyboards that don't
     * support Bluetooth 2.1 Secure Simple Pairing, in contrast to DisplayPasskey
     * which is used for those that do. This method will only ever be called once
     * since older keyboards do not support typing notification. Note that the PIN
     * will always be a 6-digit number, zero-padded to 6 digits. This is for harmony
     * with the later specification. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled -->
     * 
     */
    @Override
    public void DisplayPinCode(Path path, String pincode) {
        log.info("DisplayPinCode {} {}", path, pincode);

    }

    /**
     * This method gets called when the service daemon needs to get the passkey for
     * an authentication. The return value should be a numeric value between
     * 0-999999. Possible errors: org.bluez.Error.Rejected org.bluez.Error.Canceled
     * 
     * @param device
     * @return
     */
    @Override
    public UInt32 RequestPasskey(Path path) {
        log.info("Path: {}", path.getPath());
        IBluezBluetoothDevice device = new BluezBluetoothDevice(bluezProvider, path.getPath());
        device.setTrusted(true);
        log.info("Set trusted");
        device.connect();
        log.info("Requestpasskey - connect");
        return new UInt32(bluezPinCode);
    }

    /**
     * This method gets called when the service daemon needs to display a passkey
     * for an authentication. The entered parameter indicates the number of already
     * typed keys on the remote side. An empty reply should be returned. When the
     * passkey needs no longer to be displayed, the Cancel method of the agent will
     * be called. During the pairing process this method might be called multiple
     * times to update the entered value. Note that the passkey will always be a
     * 6-digit number, so the display should be zero-padded at the start if the
     * value contains less than 6 digits.
     * 
     * @param device
     * 
     * @param passkey
     * 
     * @param entered
     */
    @Override
    public void DisplayPassKey(Path path, UInt32 passkey, UInt16 entered) {
        log.info("DisplayPassKey {} {} {}", path, passkey, entered);
    }

    /**
     * This method gets called when the service daemon needs to confirm a passkey
     * for an authentication. To confirm the value it should return an empty reply
     * or an error in case the passkey is invalid. Note that the passkey will always
     * be a 6-digit number, so the display should be zero-padded at the start if the
     * value contains less than 6 digits. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled
     * 
     * @param device
     * @param passkey
     */
    @Override
    public void RequestConfirmation(Path path, UInt32 passkey) {
        log.info("RequestConfirmation {} {}", path, passkey);
    }

    /**
     * 
     * This method gets called to request the user to authorize an incoming
     * 
     * pairing attempt which would in other circumstances trigger the just-works
     * model. Possible errors: org.bluez.Error.Rejected org.bluez.Error.Canceled
     * 
     * @param device
     */
    @Override
    public void RequestAuthorization(Path path) {
    }

    /**
     * This method gets called when the service daemon needs to authorize a
     * connection/service request. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled
     * 
     * @param device
     * @param uuid
     */
    @Override
    public void AuthorizeService(Path path, String uuid) {
        log.info("AuthorizeService {} {}", path, uuid);
    }

    /**
     * This method gets called to indicate that the agent request failed before a
     * reply was returned.
     * 
     */
    @Override
    public void Cancel() {
        log.info("Cancel called");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.freedesktop.dbus.DBusInterface#isRemote()
     */
    @Override
    public boolean isRemote() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.freedesktop.dbus.DBusInterface#getObjectPath()
     */
    @Override
    public String getObjectPath() {
        return bluezAgentObjectPath;
    }

}