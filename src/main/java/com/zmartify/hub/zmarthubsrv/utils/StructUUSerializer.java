package com.zmartify.hub.zmarthubsrv.utils;

import java.io.IOException;

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

public class StructUUSerializer extends StdSerializer<StructUU> {

    private static final long serialVersionUID = 1L;

    public StructUUSerializer() {
        this(null);
    }

    public StructUUSerializer(Class<StructUU> t) {
        super(t);
    }

    public void serialize(StructUU src, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        jgen.writeObject(src.a);
        jgen.writeObject(src.b);
        jgen.writeEndArray();
    }

}