/**
 *
 */
package org.freedesktop;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Tuple;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zmartify.hub.zmarthubsrv.utils.PairSerializer;

/**
 * @author Peter Kristensen
 *
 */
@JsonSerialize(using = PairSerializer.class)
public final class Pair<A, B> extends Tuple {
    @Position(0)
    public final A a;
    @Position(1)
    public final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
