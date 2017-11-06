/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.jettyclient;

import org.eclipse.jetty.http.HttpFields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Peter Kristensen
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JettyResponse extends AbstractJettyResponse {

    public JettyResponse(String reqId) {
        super();
        this.reqId = reqId;
    }

    public JettyResponse(JettyRequest jettyRequest) {
        super();
        this.reqId = jettyRequest.getReqId();
        this.method = jettyRequest.getMethod();
        this.headers = new HttpFields();
        // this.uri = jettyRequest.getUri();
    }
}