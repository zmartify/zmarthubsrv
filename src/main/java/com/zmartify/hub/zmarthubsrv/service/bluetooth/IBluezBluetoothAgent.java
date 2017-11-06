/**
 *
 */
package com.zmartify.hub.zmarthubsrv.service.bluetooth;

import org.bluez.Agent1;

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

/**
 * A Bluetooth device.
 *
 * @author Keith M. Hughes
 */
public interface IBluezBluetoothAgent extends Agent1 {

    String getPinCode();

    void setPinCode(String PinCode);

}