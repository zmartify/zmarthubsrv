/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.Map;

import org.freedesktop.dbus.Variant;

/**
 * @author Peter Kristensen
 *
 */

public class ZmartAccessPoint {
    private final String path;
    private final Map<String, Variant<?>> properties;

    public ZmartAccessPoint(String path, Map<String, Variant<?>> properties) {
        super();
        this.path = path;
        this.properties = properties;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Variant<?>> getProperties() {
        return properties;
    }
}
