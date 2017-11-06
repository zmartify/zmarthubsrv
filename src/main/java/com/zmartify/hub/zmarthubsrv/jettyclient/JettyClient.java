package com.zmartify.hub.zmarthubsrv.jettyclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.BufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zmartify.hub.zmarthubsrv.IStandardClient;
import com.zmartify.hub.zmarthubsrv.bluetooth.Message;
import com.zmartify.hub.zmarthubsrv.bluetooth.MessageListener;

/**
 * @author Peter Kristensen
 *
 */
public class JettyClient implements IStandardClient {

    /*
     * Logger for this class
     */
    protected static Logger log = LoggerFactory.getLogger(JettyClient.class);
    /*
     * This constant defines maximum number of HTTP connections per peer address for
     * HTTP client which performs local connections to watson
     */
    private static final int HTTP_CLIENT_MAX_CONNECTIONS_PER_DEST = 200;

    /*
     * This constant defines HTTP request timeout. It should be kept at about 30
     * seconds minimum to make it work for long polling requests
     */
    private static final int HTTP_CLIENT_TIMEOUT = 30000;

    /*
     * Controls if headers should be part of the response
     */
    // private static final boolean SEND_HEADER = false;

    /*
     * This hashmap holds HTTP requests to local openHAB which are currently running
     */
    public HashMap<String, Request> runningRequests;

    /*
     * This variable holds instance of Jetty HTTP client to make requests to local
     * watson
     */
    private HttpClient jettyClient;

    private MessageListener subscriber = null;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public JettyClient() {
        super();
        runningRequests = new HashMap<String, Request>();

        jettyClient = new HttpClient();

        jettyClient.setMaxConnectionsPerDestination(HTTP_CLIENT_MAX_CONNECTIONS_PER_DEST);
        jettyClient.setConnectTimeout(HTTP_CLIENT_TIMEOUT);

        try {
            jettyClient.start();
            log.info("Jetty Client started..");
        } catch (Exception e) {
            log.error("Error starting Jetty Client");
        }
    }

    @Override
    public void startup() {
        try {
            jettyClient.start();
            log.info("Jetty Client started..");
        } catch (Exception e) {
            log.error("Error starting Jetty Client");
        }
    }

    @Override
    public void shutdown() {
        if (jettyClient.isStarted()) {
            try {
                jettyClient.stop();
                log.info("Jetty Client shutdown..");
            } catch (Exception e) {
                log.error("Error stopping Jetty Client");
            }
        }

    }

    private void sendErrorMessage(JettyResponse jettyResponse, int errorCode, String reason) {
        jettyResponse.setStatusCode(errorCode);
        jettyResponse.setStatusText(reason);
        try {
            subscriber.message(new Message(jsonMapper.writeValueAsBytes(jettyResponse)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error message - returning null");
        }
    }

    private void sendErrorMessage(JettyResponse jettyResponse, int errorCode) {
        sendErrorMessage(jettyResponse, errorCode, HttpStatus.getMessage(errorCode));
    }

    private void sendContentMessage(JettyResponse jettyResponse, String responseString, int statusCode) {
        try {
            jettyResponse.setBody(jsonMapper.readTree(responseString.toString()));
            jettyResponse.setStatusCode(statusCode);
            jettyResponse.setStatusText(HttpStatus.getMessage(statusCode));

            subscriber.message(new Message(jsonMapper.writeValueAsBytes(jettyResponse)));

            log.info("Content received ({})\n{}", runningRequests.size(),
                    jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jettyResponse));

        } catch (IOException e) {
            log.error("Error serializing content - nothing returned");
            sendErrorMessage(jettyResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
        }
    }

    private void addRequestQueue(String reqId, Request request) {
        runningRequests.put(reqId, request);
    }

    private void removeRequestQueue(String reqId) {
        if (runningRequests.containsKey(reqId)) {
            runningRequests.remove(reqId);
        }
    }

    @Override
    public void handleRequest(JettyRequest jettyRequest) {

        try {
            log.info("Jetty handling request");
            // If no reqId specified, we create one
            if (jettyRequest.getReqId() == null) {
                jettyRequest.setReqId(UUID.randomUUID().toString());
            }

            Request request = jettyClient.newRequest(jettyRequest.getUri().toURI());
            jettyRequest.getHeaders().forEach(header -> {
                request.header(header.getHeader(), header.getValue());
            });
            request.method(jettyRequest.getMethod());

            addRequestQueue(jettyRequest.getReqId(), request);

            StringBuffer responseString = new StringBuffer();

            JettyResponse jettyResponse = new JettyResponse(jettyRequest);

            request
                    // .onRequestQueued(req -> { log.info("Request queued {}", req); )
                    // .onRequestBegin(req -> { log.info("Request Begin {}", req); })
                    // .onResponseBegin(response -> { log.info("Response Begin {}", response); })
                    // .onResponseHeaders(res -> { if (SEND_HEADER)
                    // jettyResponse.setHeaders(res.getHeaders()); })
                    .onResponseContent((response, buffer) -> {
                        responseString.append(new String(BufferUtil.toArray(buffer)));
                    }).onResponseFailure((res, throwable) -> {
                        sendErrorMessage(jettyResponse, res.getStatus(), res.getReason());
                    }).onComplete(req -> {
                        if (req.isFailed() && req.getResponse().getStatus() != HttpStatus.OK_200) {
                            sendErrorMessage(jettyResponse, req.getResponse().getStatus());
                        } else {
                            sendContentMessage(jettyResponse, responseString.toString(), HttpStatus.OK_200);
                        }
                        ;
                        removeRequestQueue(jettyResponse.getReqId());
                    }).send(result -> {
                        /* log.info("Result {}", result.getResponse()); */
                    });
        } catch (URISyntaxException e) {
            log.error("URI syntax error '{}' - request skipped.");
        }
    }

    public void handleCancelEvent(String requestId) {
        log.debug("Received cancel for request {}", requestId);
        // Find and abort running request
        if (runningRequests.containsKey(requestId)) {
            Request request = runningRequests.get(requestId);
            request.abort(new InterruptedException());
            runningRequests.remove(requestId);
        }
    }

    @Override
    public void register(MessageListener listener) {
        log.debug("Register messageListener : " + listener);
        this.subscriber = listener;
    }
}
