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

public final class StructAYUAYU extends Struct {
    @Position(0)
    public final byte[] a;
    @Position(1)
    public final UInt32 b;
    @Position(2)
    public final byte[] c;
    @Position(3)
    public final UInt32 d;

    public StructAYUAYU(byte[] a, UInt32 b, byte[] c, UInt32 d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
}
