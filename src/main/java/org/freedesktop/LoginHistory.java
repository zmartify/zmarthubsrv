package org.freedesktop;

import java.util.Map;

import org.freedesktop.dbus.Struct;

public class LoginHistory extends Struct {
    public final long a;
    public final long b;
    public final Map<String, Object> c;

    public LoginHistory(long a, long b, Map<String, Object> c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

}
