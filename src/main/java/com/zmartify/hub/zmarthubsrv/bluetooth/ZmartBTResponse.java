package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZmartBTResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String reqId = null;
    private String type = null;
    @JsonProperty("status-code")
    private String statusCode = null;
    private String status = null;
    private String change = null;
    private String result = null;

    public ZmartBTResponse() {
    }

    public ZmartBTResponse(String reqId) {
        super();
        this.reqId = reqId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

}
