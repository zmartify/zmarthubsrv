/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.utils;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.UInt32;

/**
 * @author Peter Kristensen
 *
 */

public final class StructAYUAY extends Struct {
    @Position(0)
    public final byte[] a;
    @Position(1)
    public final UInt32 b;
    @Position(2)
    public final byte[] c;

    public StructAYUAY(byte[] a, UInt32 b, byte[] c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
