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

public class ZmartConnection {
    private final String path;
    private final Map<String, Map<String, Variant<?>>> properties;

    public ZmartConnection(String path, Map<String, Map<String, Variant<?>>> properties) {
        super();
        this.path = path;
        this.properties = properties;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Map<String, Variant<?>>> getProperties() {
        return properties;
    }

}
