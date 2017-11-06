/**
 *
 */
package com.zmartify.hub.zmarthubsrv;

import com.zmartify.hub.zmarthubsrv.bluetooth.ZmartBTRfcommServer;

/**
 * @author peter
 *
 */
public class ZmartBTServer {

    static Thread serverThread;

    /**
     * @param args
     */
    public static void main(String[] args) {

        serverThread = new Thread(new ZmartBTRfcommServer());
        serverThread.start();
    }

}
