/**
 *
 */
package com.zmartify.hub.zmarthubsrv.hubclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
import com.zmartify.hub.zmarthubsrv.service.nwm.INWMConnection;
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
            nwmProvider.startup(false);
            // Change hostname to ZmartHUB-'serialno'
            nwmProvider.saveHostname("ZmartHUB-"+getMachineSerialNumber());
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
            log.debug("Sending message: ({})", m);

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
                    if (cmd.length > 2) {
                        switch (cmd[2]) {
                            case "active":
                            switch (jettyRequest.getMethod()) {
                                case GET:
                                    sendContentMessage(response, nwmProvider.getActiveAccessPoints(), HttpStatus.OK_200);
                                    break;
                                default:
                                    log.error("Unknown method");
                            }
                            break;
                            case "connect":
                            switch (jettyRequest.getMethod()) {
                                case GET:
                                    sendContentMessage(response, nwmProvider.connectedWifi(), HttpStatus.OK_200);
                                    break;
                                default:
                                    log.error("Unknown method");
                            }
                            break;
                            case "disconnect":
                            switch (jettyRequest.getMethod()) {
                                case PUT:
                                    sendContentMessage(response, nwmProvider.disconnectWifi(), HttpStatus.OK_200);
                                    break;
                                default:
                                    log.error("Unknown method");
                            }
                            break;

                            default:
                             log.error("AccessPoint - Unknown command {}",cmd[2]);
                        }
                    }
                    switch (jettyRequest.getMethod()) {
                        case GET:
                            sendContentMessage(response, nwmProvider.getAccessPoints(), HttpStatus.OK_200);
                            break;
                        case PUT:
                            String ap = jettyRequest.getBody().get("accesspoint").asText();
                            String pw = jettyRequest.getBody().get("secret").asText();
                            Object res = nwmProvider.connectToAP(ap, pw);
                            sendContentMessage(response, res, res != null ? HttpStatus.OK_200 : HttpStatus.UNAUTHORIZED_401);
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
                                INWMConnection connection = new NWMConnection(nwmProvider, conn.getObjectPath());
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

    public String getMachineSerialNumber() {
        String serialnum = null;
        try {
        Process process = Runtime.getRuntime().exec(new String[]{"cat","/proc/cpuinfo"});
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream());
        while (sc.hasNext()) {
            if (sc.next().equals("Serial")) {
                sc.next();
                serialnum = sc.next();
                break;
            }
        }
        sc.close();
        }
        catch (Exception e) {
            log.error("Error getting serial number :: {}", e.getMessage());
        }
        return serialnum;
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
