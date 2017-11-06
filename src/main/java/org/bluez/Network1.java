package org.bluez;

import org.freedesktop.dbus.DBusInterface;

public interface Network1 extends DBusInterface {

    public String Connect(String uuid);

    public void Disconnect();

}
