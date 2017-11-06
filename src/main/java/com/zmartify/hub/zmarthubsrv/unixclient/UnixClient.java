/**
 *
 */
package com.zmartify.hub.zmarthubsrv.unixclient;
/*
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocketClient;
// import com.etsy.net.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zmartify.hub.zmarthubsrv.IStandardClient;
import com.zmartify.hub.zmarthubsrv.bluetooth.Message;
import com.zmartify.hub.zmarthubsrv.bluetooth.MessageListener;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyRequest;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyResponse;

public class UnixClient implements IStandardClient {

    private static final Logger log = LoggerFactory.getLogger(UnixClient.class);

    private static final String NEWLINE = "\r\n";

    private static final String SOCKET_FILE = "/run/snapd.socket";

    private UnixDomainSocketClient socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public HashMap<String, Request> runningRequests;

    static ObjectMapper jsonMapper = new ObjectMapper();

    private MessageListener subscriber = null;

    private boolean running = false;

    public UnixClient() {
        super();
        runningRequests = new HashMap<String, Request>();
    }

    @Override
    public void startup() {
        try {
            // Create socket connection
            socket = new UnixDomainSocketClient(SOCKET_FILE, JUDS.SOCK_STREAM);
            // socket = new Socket("/run/snapd.socket", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;
            log.info("Unix Client started..");
        } catch (Exception e) {
            log.error("Error starting Unix Client");
        }
    }

    @Override
    public void shutdown() {
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            log.error("IO error trying to close input stream");
        }
        socket.close();
        log.info("Unix client shutdown.");
        running = false;
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

    @SuppressWarnings("unused")
    private void sendErrorMessage(JettyResponse jettyResponse, int errorCode) {
        sendErrorMessage(jettyResponse, errorCode, HttpStatus.getMessage(errorCode));
    }

    @SuppressWarnings("unused")
    private void addRequestQueue(String reqId, Request request) {
        runningRequests.put(reqId, request);
    }

    @SuppressWarnings("unused")
    private void removeRequestQueue(String reqId) {
        if (runningRequests.containsKey(reqId)) {
            runningRequests.remove(reqId);
        }
    }

    @Override
    public void handleRequest(JettyRequest jettyRequest) {

        try {
            // If no reqId specified, we create one
            if (jettyRequest.getReqId() == null) {
                jettyRequest.setReqId(UUID.randomUUID().toString());
            }

            StringBuffer request = new StringBuffer(jettyRequest.getMethod().toString()).append(" ");
            request.append(jettyRequest.getUri().getPath()).append(" HTTP/1.1").append(NEWLINE);
            request.append("Host: ").append(jettyRequest.getUri().getScheme()).append(NEWLINE);
            // jettyRequest.getHeaders().forEach(header -> {
            // request.append(header.toString()).append(NEWLINE); });

            // Mark end of request
            // request.append(NEWLINE);

            log.info("Request: \n{}", request.toString());

            JettyResponse jettyResponse = new JettyResponse(jettyRequest);

            out.print(request.toString());
            out.println("");

            boolean completed = false;

            // Received basic response header
            String recBuf = in.readLine();
            log.info("Read: {}", recBuf);

            // Start receiving headers
            do {
                String line = in.readLine();

                if (!line.isEmpty()) {
                    // Assume header and store it
                    int i = line.indexOf(':');
                    log.info("HEADER: {} ({} / {})", line, i, line.length());
                    jettyResponse.getHeaders().add(line.substring(0, i), line.substring(i + 1));
                }

                if (line.startsWith("Content-Length:")) {
                    int l = Integer.valueOf(line.substring(16));

                    // Skip the empty line
                    line = in.readLine();

                    StringBuffer sb = new StringBuffer();
                    log.info("Lenght {}", l);

                    int remain = l;
                    while (!completed) {
                        char[] cbuf = new char[l];
                        in.read(cbuf, 0, remain);
                        remain = remain - cbuf.length;
                        sb.append(cbuf);
                        completed = (remain <= 0);
                    }
                    jettyResponse.setUbuntuResponse(jsonMapper.readTree(sb.toString()));
                }
            } while (!completed);

            log.info("Response: \n{}\n", jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jettyResponse));

            if (subscriber != null) {
                subscriber.message(new Message(jsonMapper.writeValueAsString(jettyResponse)));
            }

        } catch (JsonParseException e) {
            log.error("Message -> JSON Parse Exception");
        } catch (JsonMappingException e) {
            log.error("Message -> JSON Mapping Exception");
        } catch (IOException e) {
            log.error("IO Exception :: {}", e.getMessage());
            e.printStackTrace();
        }

    }

    // out.println("GET " + cmd + " HTTP/1.1");
    // out.println("Host: http");
    // out.println("Accept: */*");
    // out.println("");

    public boolean isRunning() {
        return running;
    }

    @Override
    public void register(MessageListener listener) {
        log.debug("Register messageListener : {}", listener);
        this.subscriber = listener;
    }
}