package com.zmartify.hub.zmarthubsrv.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class VariantSerializer extends StdSerializer<Variant> {

    private JsonGenerator jgen = null;

    private static final Logger log = LoggerFactory.getLogger(VariantSerializer.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public VariantSerializer() {
        this(null);
    }

    public VariantSerializer(Class<Variant> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    private void writeStruct(Variant src) throws IOException {
        switch (src.getSig()) {
            case "(ayuay)":
                jgen.writeObject(((Variant<StructAYUAY>) src).getValue());
                break;
            case "(ayuayu)":
                jgen.writeObject(((Variant<StructAYUAYU>) src).getValue());
                break;
            case "(uu)":
                jgen.writeObject(((Variant<StructUU>) src).getValue());
                break;
            default: {
                log.error("ERROR! Unsupported structure {}", src.getSig());
            }
        }

    }

    @SuppressWarnings({ "unchecked" })
    public void serialize(Variant src, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        this.jgen = jgen;

        // log.info("Serialize: sig={}, type={}, value={} ({})", src.getSig(),
        // src.getType(), src.getValue(), this);

        switch (src.getSig().charAt(0)) {
            case 'y':
                jgen.writeNumber(((Variant<Byte>) src).getValue() & 0xFF);
                break;
            case 'a': // Array
                switch (src.getSig()) {
                    case "ay":
                        byte[] b = ((byte[]) src.getValue());
                        jgen.writeStartArray();
                        for (int i = 0; i < b.length; i++)
                            jgen.writeNumber(b[i] & 0xFF);
                        jgen.writeEndArray();
                        break;
                    case "a{ss}":
                    case "a{sv}":
                        jgen.writeObject((Map<String, Variant<?>>) src.getValue());
                        break;
                    default:
                        jgen.writeObject((Vector<Variant<?>>) src.getValue());
                        break;
                }
                break;
            case '(': // Structure start
                writeStruct(src);
                break;
            default:
                jgen.writeObject(src.getValue());
        }
    }

}