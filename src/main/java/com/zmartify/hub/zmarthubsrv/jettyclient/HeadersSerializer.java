/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.jettyclient;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Peter Kristensen
 *
 */
public class HeadersSerializer extends StdSerializer<HttpFields> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public HeadersSerializer() {
        this(null);
    }

    protected HeadersSerializer(Class<HttpFields> t) {
        super(t);
    }

    @Override
    public void serialize(HttpFields headers, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        // if (headers.size() == 0) return;

        jgen.writeStartObject();

        Iterator<HttpField> hlist = headers.iterator();
        while (hlist.hasNext()) {
            HttpField header = hlist.next();
            // jgen.writeStringField(header.getHeader().name(), header.getValue());
            jgen.writeStringField(HttpHeader.valueOf(header.getHeader().name()).toString(), header.getValue());
        }

        jgen.writeEndObject();

    }
}
