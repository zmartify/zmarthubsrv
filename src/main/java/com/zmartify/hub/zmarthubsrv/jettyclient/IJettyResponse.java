package com.zmartify.hub.zmarthubsrv.jettyclient;

import org.eclipse.jetty.http.HttpFields;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Peter Kristensen
 *
 */
public interface IJettyResponse extends IJettyRequest {

    public void setHeaders(HttpFields headers);

    public void setBody(JsonNode body);

    public int getStatusCode();

    public void setStatusCode(int statusCode);

    public String getStatusText();

    public void setStatusText(String statusText);

}
