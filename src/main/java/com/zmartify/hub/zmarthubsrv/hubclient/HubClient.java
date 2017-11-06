/**
 *
 */
package com.zmartify.hub.zmarthubsrv.hubclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zmartify.hub.zmarthubsrv.IStandardClient;
import com.zmartify.hub.zmarthubsrv.bluetooth.Message;
import com.zmartify.hub.zmarthubsrv.bluetooth.MessageListener;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyRequest;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyResponse;
import com.zmartify.hub.zmarthubsrv.service.nwm.INWMProvider;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMConnection;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMProvider;
import com.zmartify.hub.zmarthubsrv.service.nwm.ZmartConnection;
import com.zmartify.hub.zmarthubsrv.utils.VariantSerializer;

/**
 * @author Peter Kristensen
 *
 */
public class HubClient implements IStandardClient {

    private static final Logger log = LoggerFactory.getLogger(HubClient.class);

    private ObjectMapper jsonMapper;

    private INWMProvider nwmProvider;

    private MessageListener subscriber = null;

    public HubClient() {
        nwmProvider = new NWMProvider();

        jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new VariantSerializer(Variant.class));
        jsonMapper.registerModule(module);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.IStandardClient#startup()
     */
    @Override
    public void startup() {
        try {
            nwmProvider.startup();
        } catch (Exception e) {
            log.error("Error trying to start NetworkManager Provider");
            return;
        }
        log.info("HubClient started.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.zmartify.hub.zmartbtserver.IStandardClient#shutdown()
     */
    @Override
    public void shutdown() {

        try {
            nwmProvider.shutdown();
        } catch (Exception e) {
            log.error("Error trying to stop NetworkManager Provider");
            return;
        }
        log.info("HubClient stopped");
    }

    private void sendErrorMessage(JettyResponse jettyResponse, int errorCode, String reason) {
        jettyResponse.setStatusCode(errorCode);
        jettyResponse.setStatusText(reason);
        try {
            subscriber.message(new Message(jsonMapper.writeValueAsString(jettyResponse)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error message - returning null");
        }
    }

    private void sendErrorMessage(JettyResponse jettyResponse, int errorCode) {
        sendErrorMessage(jettyResponse, errorCode, HttpStatus.getMessage(errorCode));
    }

    private void sendContentMessage(JettyResponse jettyResponse, Object response, int statusCode) {
        try {
            jettyResponse.setBody(jsonMapper.valueToTree(response));
            jettyResponse.setStatusCode(statusCode);
            jettyResponse.setStatusText(HttpStatus.getMessage(statusCode));

            Message m = new Message(jsonMapper.writeValueAsString(jettyResponse));
            log.info("Sending message: ({})", m);

            subscriber.message(m);

        } catch (IOException e) {
            log.error("Error serializing content - nothing returned");
            sendErrorMessage(jettyResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.IStandardClient#handleRequest(com.zmartify.hub
     * .zmartbtsever.jettyclient.JettyRequest)
     */
    @Override
    public void handleRequest(JettyRequest jettyRequest) {
        log.info("HubClient got request : {} ({})", jettyRequest.getUri().getPath(), jettyRequest.getReqId());

        String[] cmd = jettyRequest.getUri().getPath().split("/");

        JettyResponse response = new JettyResponse(jettyRequest);

        try {

            switch (cmd[1]) {
                case "accesspoints":
                    switch (jettyRequest.getMethod()) {
                        case GET:
                            sendContentMessage(response, nwmProvider.getWireless().getAPs(), HttpStatus.OK_200);
                            break;
                        case PUT:
                            String ap = jettyRequest.getBody().get("accesspoint").asText();
                            String pw = jettyRequest.getBody().get("secret").asText();
                            sendContentMessage(response, nwmProvider.connectToAP(ap, pw), HttpStatus.OK_200);
                            break;
                        default:
                            log.error("Unknown method");
                    }
                    break;
                case "connections":
                    switch (jettyRequest.getMethod()) {
                        case GET:
                            List<ZmartConnection> connections = new ArrayList<ZmartConnection>();
                            nwmProvider.listConnections().forEach(conn -> {
                                NWMConnection connection = new NWMConnection(nwmProvider, conn.getObjectPath());
                                connections.add(new ZmartConnection(conn.getObjectPath(),
                                        connection.getConnection().GetSettings()));
                            });
                            sendContentMessage(response, connections, HttpStatus.OK_200);
                            break;
                        default:
                            log.error("Unknown method");
                    }
                    break;

                default:
                    sendErrorMessage(response, HttpStatus.BAD_REQUEST_400);
            }
        } catch (DBusExecutionException e1) {
            sendErrorMessage(response, HttpStatus.UNAUTHORIZED_401, e1.getMessage());
        } catch (Exception e2) {
            sendErrorMessage(response, HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        log.info("HubClient got request : {}", jettyRequest.getUri().getPath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.zmartify.hub.zmartbtserver.IStandardClient#register(com.zmartify.hub.
     * zmartbtserver.bluetooth.MessageListener)
     */
    @Override
    public void register(MessageListener listener) {
        this.subscriber = listener;
    }

}
