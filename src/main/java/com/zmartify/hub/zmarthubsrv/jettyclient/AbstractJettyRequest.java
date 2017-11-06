/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.jettyclient;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpURI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Peter Kristensen
 *
 */

abstract public class AbstractJettyRequest implements IJettyRequest {

    protected String reqId;
    protected HttpMethod method;

    @JsonProperty("url")
    protected HttpURI uri;

    @JsonSerialize(using = HeadersSerializer.class)
    @JsonDeserialize(using = HeadersDeserializer.class)
    protected HttpFields headers = new HttpFields();

    @JsonProperty("result")
    protected JsonNode body;

    public AbstractJettyRequest() {
        super();
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public HttpURI getUri() {
        return uri;
    }

    public JsonNode getBody() {
        return body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpFields getHeaders() {
        return headers;
    }

    public void setHeaders(HttpFields headers) {
        this.headers = headers;
    }

}
