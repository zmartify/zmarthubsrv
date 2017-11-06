package com.zmartify.hub.zmarthubsrv.bluetooth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zmartify.hub.zmarthubsrv.jettyclient.AbstractJettyRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZmartBTRequest extends AbstractJettyRequest {

}
