package org.freedesktop;

import org.freedesktop.dbus.DBusInterface;

public interface Peer extends DBusInterface {

    public String getMachineId();

}
