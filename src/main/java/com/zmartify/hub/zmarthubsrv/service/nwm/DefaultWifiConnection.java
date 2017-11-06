/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.freedesktop.dbus.Variant;

/**
 * @author Peter Kristensen
 *
 */
public class DefaultWifiConnection {

    private Map<String, Map<String, Variant<?>>> connection = new HashMap<String, Map<String, Variant<?>>>();

    public DefaultWifiConnection(String ssid, String password) {
        super();
        Map<String, Variant<?>> sConnection = new HashMap<String, Variant<?>>();
        sConnection.put("type", new Variant<String>("802-11-wireless"));
        sConnection.put("uuid", new Variant<String>(UUID.randomUUID().toString()));
        sConnection.put("id", new Variant<String>("Wifi " + ssid));

        Map<String, Variant<?>> sWifi = new HashMap<String, Variant<?>>();
        sWifi.put("ssid", new Variant<byte[]>(ssid.getBytes()));
        sWifi.put("mode", new Variant<String>("infrastructure"));

        Map<String, Variant<?>> sWifiSecurity = new HashMap<String, Variant<?>>();
        sWifiSecurity.put("key-mgmt", new Variant<String>("wpa-psk"));
        sWifiSecurity.put("auth-alg", new Variant<String>("open"));
        sWifiSecurity.put("psk", new Variant<String>(password));

        Map<String, Variant<?>> sIp4 = new HashMap<String, Variant<?>>();
        sIp4.put("method", new Variant<String>("auto"));

        Map<String, Variant<?>> sIp6 = new HashMap<String, Variant<?>>();
        sIp6.put("method", new Variant<String>("ignore"));

        connection.put("connection", sConnection);
        connection.put("802-11-wireless", sWifi);
        connection.put("802-11-wireless-security", sWifiSecurity);
        connection.put("ipv4", sIp4);
        connection.put("ipv6", sIp6);
    }

    public void setUUID(String uuid) {
        connection.get("connection").replace("uuid", new Variant<String>(uuid));
    }

    public Map<String, Map<String, Variant<?>>> get() {
        return connection;
    }
}
