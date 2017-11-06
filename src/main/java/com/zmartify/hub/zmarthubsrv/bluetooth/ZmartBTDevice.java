package com.zmartify.hub.zmarthubsrv.bluetooth;

import java.util.UUID;

public class ZmartBTDevice {
    protected String uniqueDeviceId;
    protected String readableName = null;
    protected String macAddress = null;

    public ZmartBTDevice() {
        this(UUID.randomUUID().toString());
    }

    public ZmartBTDevice(String uniqueId) {
        this.uniqueDeviceId = uniqueId;
    }

    public String getUniqueDeviceID() {
        return uniqueDeviceId;
    }

    public String getReadableName() {
        return readableName == null || readableName.isEmpty() ? uniqueDeviceId : readableName;
    }

    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    public int compareTo(ZmartBTDevice another) {
        return this.getUniqueDeviceID().compareTo(another.getUniqueDeviceID());
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer("BlaubotDevice{");
        sb.append("uniqueDeviceId='").append(uniqueDeviceId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ZmartBTDevice))
            return false;
        return this.getUniqueDeviceID().equals(((ZmartBTDevice) o).getUniqueDeviceID());
    }

    @Override
    public int hashCode() {
        return uniqueDeviceId != null ? uniqueDeviceId.hashCode() : 0;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;

    }
}
