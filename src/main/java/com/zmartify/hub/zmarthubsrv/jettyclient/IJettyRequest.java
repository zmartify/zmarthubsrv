package com.zmartify.hub.zmarthubsrv.jettyclient;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpURI;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Peter Kristensen
 *
 */

public interface IJettyRequest {

    public String getReqId();

    public void setReqId(String reqId);

    public HttpURI getUri();

    public HttpFields getHeaders();

    public JsonNode getBody();

    public HttpMethod getMethod();
}
