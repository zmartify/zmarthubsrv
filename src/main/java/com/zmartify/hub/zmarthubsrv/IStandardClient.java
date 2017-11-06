package com.zmartify.hub.zmarthubsrv;

import com.zmartify.hub.zmarthubsrv.bluetooth.MessageListener;
import com.zmartify.hub.zmarthubsrv.jettyclient.JettyRequest;

/**
 * @author Peter Kristensen
 *
 */
public interface IStandardClient {

    public void startup();

    public void shutdown();

    public void handleRequest(JettyRequest jettyRequest);

    public void register(MessageListener listener);

}
