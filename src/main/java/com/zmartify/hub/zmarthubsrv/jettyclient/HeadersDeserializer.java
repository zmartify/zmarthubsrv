/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.jettyclient;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jetty.http.HttpFields;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Peter Kristensen
 *
 */
public class HeadersDeserializer extends StdDeserializer<HttpFields> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public HeadersDeserializer() {
        this(null);
    }

    public HeadersDeserializer(Class<?> vc) {
        super(vc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
     * com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public HttpFields deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        HttpFields headers = new HttpFields();

        JsonNode node = jp.getCodec().readTree(jp);
        Iterator<String> hdrs = node.fieldNames();
        while (hdrs.hasNext()) {
            String headerName = hdrs.next();
            String headerValue = node.get(headerName).asText();
            headers.add(headerName, headerValue);
        }
        return headers;
    }
}
