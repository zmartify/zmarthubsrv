/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.utils;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.UInt32;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Peter Kristensen
 *
 */
@JsonSerialize(using = StructUUSerializer.class)
@JsonDeserialize(using = StructUUDeserializer.class)
public final class StructUU extends Struct {
    @Position(0)
    public final UInt32 a;
    @Position(1)
    public final UInt32 b;

    public StructUU(UInt32 a, UInt32 b) {
        this.a = a;
        this.b = b;
    }
}
