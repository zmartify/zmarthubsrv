package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZmartBTUtils {
    private static final int btAttrServiceName = 0x0100;
    private final static Logger log = LoggerFactory.getLogger(ZmartBTUtils.class);

    public List<RemoteDevice> discoverDevices() {
        final Object sync = new Object();
        final List<RemoteDevice> devs = new ArrayList<RemoteDevice>();

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice dev, DeviceClass devClass) {
                try {
                    log.info("Found device: address: {}, name: {}", dev.getBluetoothAddress(),
                            dev.getFriendlyName(false));
                    devs.add(dev);
                } catch (IOException e) {
                    log.error(e.toString(), e);
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                log.info("Device inquiry completed");
                synchronized (sync) {
                    sync.notifyAll();
                }
            }

            @Override
            public void serviceSearchCompleted(int transId, int respCode) {
            }

            @Override
            public void servicesDiscovered(int transId, ServiceRecord[] servRecord) {
            }
        };

        synchronized (sync) {
            boolean started;
            try {
                started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    log.info("Device inquiry started");
                    sync.wait();
                    log.info("Devices count: {}", devs.size());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return devs;
    }

    public List<String> searchForServices(List<RemoteDevice> devices, String uuidStr) {
        final Object sync = new Object();
        final List<String> urls = new ArrayList<String>();

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice dev, DeviceClass devClass) {
            }

            @Override
            public void inquiryCompleted(int discType) {
            }

            @Override
            public void servicesDiscovered(int transId, ServiceRecord[] servRecord) {
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url != null) {
                        urls.add(url);
                        DataElement name = servRecord[i].getAttributeValue(btAttrServiceName);
                        log.info("Service found: url: {}, name: {}", url, name);
                    }
                }
            }

            @Override
            public void serviceSearchCompleted(int transId, int respCode) {
                log.info("Service search completed");
                synchronized (sync) {
                    sync.notifyAll();
                }
            }

        };

        UUID[] uuidArr = new UUID[] { new UUID(uuidStr, false) };
        int[] attrIds = new int[] { btAttrServiceName };

        for (RemoteDevice device : devices) {
            synchronized (sync) {
                try {
                    log.info("Searching for service: {} on device: {} / {}",
                            new Object[] { uuidStr, device.getBluetoothAddress(), device.getFriendlyName(false) });
                    LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIds, uuidArr, device, listener);
                    sync.wait();
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
            }
        }

        return urls;
    }
}
