package com.zmartify.hub.zmarthubsrv.utils;

import java.io.IOException;

import org.freedesktop.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson Json serializer in order to correctly support byte[] and custom
 * Struct
 * 
 * @author Peter Kristensen
 *
 */

@SuppressWarnings("rawtypes")
public class PairSerializer extends StdSerializer<Pair> {

    private static final long serialVersionUID = 1L;

    public PairSerializer() {
        this(null);
    }

    public PairSerializer(Class<Pair> t) {
        super(t);
    }

    public void serialize(Pair src, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        jgen.writeObject(src.a);
        jgen.writeObject(src.b);
        jgen.writeEndArray();
    }

}