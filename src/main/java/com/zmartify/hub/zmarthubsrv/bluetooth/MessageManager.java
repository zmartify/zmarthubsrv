package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zmartify.hub.zmarthubsrv.hubclient.HubClient;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyClient;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyRequest;
import com.zmartify.hub.zmarthubsrv.unixclient.UnixClient;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Receives all messages from the message queue and dispatches them to
 * the various clients (i.e. service providers)
 *
 * Clients are responsible for sending any response back via
 * the subscribed 'MessageListener (sendQueue)'
 *
 * @author Peter Kristensen
 *
 */
public class MessageManager {
    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);

    private AtomicInteger receivedMessages = new AtomicInteger(0);

    private ObjectMapper jsonMapper = new ObjectMapper();

    protected MessageListener subscriber = null;

    protected JettyClient jettyClient = new JettyClient();
    protected UnixClient unixClient = new UnixClient();
    protected HubClient hubClient = new HubClient();

    protected transient boolean running = false;

    public MessageManager() {
    }

    public void startup() {
        jettyClient.startup();
        unixClient.startup();
        hubClient.startup();
    }

    public void shutdown() {
        hubClient.shutdown();
        unixClient.shutdown();
        jettyClient.shutdown();
    }

    public void register(MessageListener listener) {
        this.subscriber = listener;
        jettyClient.register(listener);
        unixClient.register(listener);
        hubClient.register(listener);
    }

    public Observer<Message> messageListener() {
        return new Observer<Message>() {

            @Override
            public void onSubscribe(Disposable d) {
                log.debug(" onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }

            @Override
            public void onError(Throwable arg0) {
                log.debug("onError");
            }

            @Override
            public void onNext(Message message) {

                log.info("We got a message (" + receivedMessages.incrementAndGet() + ") : " + message.toString() + " : "
                        + new String(message.getPayload()));

                JettyRequest request;
                try {
                    request = jsonMapper.readValue(message.getPayload(), JettyRequest.class);

                    // log.info("MM: \n{}\n", jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

                    switch (request.getUri().getHost()) {
                        case "snap": // Snap daemon
                            unixClient.handleRequest(request);
                            break;
                        case "zmarthub":
                            hubClient.handleRequest(request);
                            break;
                        default:
                            jettyClient.handleRequest(request);
                    }

                } catch (IOException e) {
                    log.error("Error parsing message - skipped! :: {}", e.getMessage());
                }

            }

        };
    }
}
