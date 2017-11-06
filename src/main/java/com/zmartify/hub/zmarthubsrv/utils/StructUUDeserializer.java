/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.utils;

import java.io.IOException;

import org.freedesktop.dbus.UInt32;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Peter Kristensen
 *
 */
public class StructUUDeserializer extends StdDeserializer<StructUU> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StructUUDeserializer() {
        this(null);
    }

    public StructUUDeserializer(Class<?> vc) {
        super(vc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.
     * jackson.core.JsonParser,
     * com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public StructUU deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        return new StructUU(new UInt32(node.get(0).asLong()), new UInt32(node.get(1).asLong()));
    }
}
