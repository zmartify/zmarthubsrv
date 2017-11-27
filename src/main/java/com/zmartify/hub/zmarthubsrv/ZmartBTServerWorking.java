/**
 *
 */
package com.zmartify.hub.zmarthubsrv;

import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.IBluezBluetoothDevice;
import com.zmartify.hub.zmarthubsrv.service.bluetooth.bluez5.BluezBluetoothProvider;
import com.zmartify.hub.zmarthubsrv.service.nwm.INWMConnectionActive;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMConnection;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMConnectionActive;
import com.zmartify.hub.zmarthubsrv.service.nwm.NWMProvider;
import com.zmartify.hub.zmarthubsrv.utils.VariantSerializer;

/**
 * @author peter
 *
 */
public class ZmartBTServerWorking {

    private static final Logger log = LoggerFactory.getLogger(ZmartBTServerWorking.class);

    static Thread serverThread;

    /**
     * @param args
     */
    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new VariantSerializer(Variant.class));
        mapper.registerModule(module);

        BluezBluetoothProvider bluezProvider = new BluezBluetoothProvider();
        NWMProvider nwmProvider = new NWMProvider();

        boolean BLUETOOTH = false;
        boolean WIRELESS = true;
        boolean TESTCONNECTION = false;

        try {
            if (BLUETOOTH) {
                bluezProvider.startup();

                log.info("Adapters: {}", bluezProvider.listAdapters());
                log.info("Devices:  {}", bluezProvider.listDevices());
                log.info("Agents:   {}", bluezProvider.listAgents());
            }

            if (WIRELESS) {
                nwmProvider.startup(false);
 
                Path activePath = null;
                
                log.info("Active Connections");
                for (Path activeConn : nwmProvider.getActiveConnections()) {
                    log.info("- connection: {}\n", activeConn.getPath());
                    // NWMConnection connection = new NWMConnection(nwmProvider, activeConnection.getPath());
                    try {
                        INWMConnectionActive activeConnection = new NWMConnectionActive(nwmProvider, activeConn.getPath());
                        log.info("Connection: \n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(activeConnection.getAll()));
                        activePath = activeConnection.getSpecificObject();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                log.info("All Connections");
                nwmProvider.listConnections().forEach(conn -> {
                    log.info("- connection: {}", conn);
                     NWMConnection connection = new NWMConnection(nwmProvider, conn.getObjectPath());
                    try {
                         connection.startup();
                        log.info("Connection: \n{}", mapper.writerWithDefaultPrettyPrinter()
                               .writeValueAsString(connection.getConnection().GetSettings()));
                        connection.shutdown();
                        connection.getConnection().Delete();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });

                // log.info("DevicesWireless:  {}", nwmProvider.getWifiDevices());

                log.info("AccessPoints         {} :\n{}", activePath.getPath(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nwmProvider.getWireless().getAPs()));
                log.info("Active AccessPoints  {} :\n{}", activePath.getPath(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nwmProvider.getWireless().getActiveAPs()));
                
                        /*
                        if (nwmProvider.connectedWifi()) {
                            log.info("Wireless is connected: {}");
                        }
                        log.info("Disconnecting wifi");
                        nwmProvider.disconnectWifi();
                        log.info("Wireless is connected: {}",nwmProvider.connectedWifi());
                        */

                if (TESTCONNECTION) {
                    nwmProvider.listConnections().forEach(conn -> {
                        NWMConnection connection = new NWMConnection(nwmProvider, conn.getObjectPath());
                        try {
                            // connection.startup();
                            log.info("Connection: \n{}", mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(connection.getConnection().GetSettings()));
                            // Thread.sleep(2000);
                            // connection.shutdown();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                }

                if (TESTCONNECTION) {
                    log.info("Active accessPoint: {}", nwmProvider.getWireless().getActiveAccessPoint());
                 
                    
                    nwmProvider.getWireless().getAllAccessPoints().forEach(ap -> {
                        try {
                            log.info("*** -->>>>Trying to connect to new AccessPoint {}",
                                    nwmProvider.connectToAP(ap.getObjectPath(), "CarpeDiem"));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        log.info("Active accessPoint: {}", nwmProvider.getWireless().getActiveAccessPoint());
                    });

                    nwmProvider.getWireless().getAllAccessPoints().forEach(ap -> {
                        try {
                            log.info("*** -->>>>Trying to connect to new AccessPoint wrong password {}",
                                    nwmProvider.connectToAP(ap.getObjectPath(), "CarpeDied"));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });

                    log.info("Active accessPoint: {}", nwmProvider.getWireless().getActiveAccessPoint());
                }
            }

            if (BLUETOOTH) {
                bluezProvider.getBluezAdapter().startScanning();

                log.info("Now scanning");

                String address = "B0:B4:48:BD:D0:83";
                if (args.length > 0) {
                    address = args[0];
                }

                IBluezBluetoothDevice device = bluezProvider.getBluezAdapter().newDevice(address);

                device.startup();

                log.info("Adapters: {}", bluezProvider.listAdapters());
                log.info("Devices:  {}", bluezProvider.listDevices());
            }

            if (BLUETOOTH) {
                bluezProvider.shutdown();
            }

            if (WIRELESS) {
                nwmProvider.shutdown();
            }

        } catch (Exception e) {
            log.error("Something BAD happened ;-) :: {}", e.getMessage());
            e.printStackTrace();
        }

        // serverThread = new Thread(new BluetoothRfcommServer());
        // serverThread.start();
    }

}
