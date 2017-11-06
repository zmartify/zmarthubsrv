/**
 * 
 */
package com.zmartify.hub.zmarthubsrv.service.nwm;

import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.UInt32;

/**
 * enums and classes related to NetworkManager connection
 * 
 * @author Peter Kristensen
 *
 */
public class NWMClass {

    /**
     * NMState values indicate the current overall networking state.
     * 
     */
    enum NMState {
        NM_STATE_UNKNOWN(0, "networking state is unknown"),
        NM_STATE_ASLEEP(10, "networking is not enabled"),
        NM_STATE_DISCONNECTED(20, "there is no active network connection"),
        NM_STATE_DISCONNECTING(30, "network connections are being cleaned up"),
        NM_STATE_CONNECTING(40, "a network connection is being started"),
        NM_STATE_CONNECTED_LOCAL(50, "there is only local IPv4 and/or IPv6 connectivity"),
        NM_STATE_CONNECTED_SITE(60, "there is only site-wide IPv4 and/or IPv6 connectivity"),
        NM_STATE_CONNECTED_GLOBAL(70, "there is global IPv4 and/or IPv6 Internet connectivity");

        private final int state;
        private final String message;

        private NMState(final int state, final String message) {
            this.state = state;
            this.message = message;
        }

        public int get() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int state) {
            return this.state == state;
        }
    }

    /**
     * NMDeviceType�values indicate the type of hardware represented by a device object.
     * 
     */
    public static enum NMDeviceType {
        NM_DEVICE_TYPE_UNKNOWN(0, "unknown device"),
        NM_DEVICE_TYPE_GENERIC(14, "generic support for unrecognized device types"),
        NM_DEVICE_TYPE_ETHERNET(1, "a wired ethernet device"),
        NM_DEVICE_TYPE_WIFI(2, "an 802.11 WiFi device"),
        NM_DEVICE_TYPE_UNUSED1(3, "not used"),
        NM_DEVICE_TYPE_UNUSED2(4, "not used"),
        NM_DEVICE_TYPE_BT(5, "a Bluetooth device supporting PAN or DUN access protocols"),
        NM_DEVICE_TYPE_OLPC_MESH(6, "an OLPC XO mesh networking device"),
        NM_DEVICE_TYPE_WIMAX(7, "an 802.16e Mobile WiMAX broadband device"),
        NM_DEVICE_TYPE_MODEM(8,
                "a modem supporting analog telephone, CDMA/EVDO, GSM/UMTS, or LTE network access protocols"),
        NM_DEVICE_TYPE_INFINIBAND(9, "an IP-over-InfiniBand device"),
        NM_DEVICE_TYPE_BOND(10, "a bond master interface"),
        NM_DEVICE_TYPE_VLAN(11, "an 802.1Q VLAN interface"),
        NM_DEVICE_TYPE_ADSL(12, "ADSL modem"),
        NM_DEVICE_TYPE_BRIDGE(13, "a bridge master interface"),
        NM_DEVICE_TYPE_TEAM(15, "a team master interface"),
        NM_DEVICE_TYPE_TUN(16, "a TUN or TAP interface"),
        NM_DEVICE_TYPE_IP_TUNNEL(17, "a IP tunnel interface"),
        NM_DEVICE_TYPE_MACVLAN(18, "a MACVLAN interface"),
        NM_DEVICE_TYPE_VXLAN(19, "a VXLAN interface"),
        NM_DEVICE_TYPE_VETH(20, "a VETH interface");

        private final int type;
        private final String message;

        private static final Map<Integer, NMDeviceType> typeByValue = new HashMap<Integer, NMDeviceType>();

        static {
            for (NMDeviceType dt : NMDeviceType.values())
                typeByValue.put(dt.type, dt);
        }

        private NMDeviceType(final int type, final String message) {
            this.type = type;
            this.message = message;
        }

        public int get() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(UInt32 type) {
            return this.type == type.intValue();
        }

        public static NMDeviceType valueOf(int type) {
            return typeByValue.get(type);
        }

        public static NMDeviceType valueOf(UInt32 type) {
            return valueOf(type.intValue());
        }
    }

    /**
     * General device capability flags.
     *
     */
    enum NMDeviceCapabilities {
        NM_DEVICE_CAP_NONE(0x00000000, "device has no special capabilities"),
        NM_DEVICE_CAP_NM_SUPPORTED(0x00000001, "NetworkManager supports this device"),
        NM_DEVICE_CAP_CARRIER_DETECT(0x00000002, "this device can indicate carrier status"),
        NM_DEVICE_CAP_IS_SOFTWARE(0x00000004, "this device is a software device");

        private final long value;
        private final String message;

        private NMDeviceCapabilities(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * 802.11 specific device encryption and authentication capabilities.
     *
     */
    enum NMDeviceWifiCapabilities {
        NM_WIFI_DEVICE_CAP_NONE(0x00000000, "device has no encryption/authentication capabilities"),
        NM_WIFI_DEVICE_CAP_CIPHER_WEP40(0x00000001, "device supports 40/64-bit WEP encryption"),
        NM_WIFI_DEVICE_CAP_CIPHER_WEP104(0x00000002, "device supports 104/128-bit WEP encryption"),
        NM_WIFI_DEVICE_CAP_CIPHER_TKIP(0x00000004, "device supports TKIP encryption"),
        NM_WIFI_DEVICE_CAP_CIPHER_CCMP(0x00000008, "device supports AES/CCMP encryption"),
        NM_WIFI_DEVICE_CAP_WPA(0x00000010, "device supports WPA1 authentication"),
        NM_WIFI_DEVICE_CAP_RSN(0x00000020, "device supports WPA2/RSN authentication"),
        NM_WIFI_DEVICE_CAP_AP(0x00000040, "device supports Access Point mode"),
        NM_WIFI_DEVICE_CAP_ADHOC(0x00000080, "device supports Ad-Hoc mode"),
        NM_WIFI_DEVICE_CAP_FREQ_VALID(0x00000100, "device reports frequency capabilities"),
        NM_WIFI_DEVICE_CAP_FREQ_2GHZ(0x00000200, "device supports 2.4GHz frequencies"),
        NM_WIFI_DEVICE_CAP_FREQ_5GHZ(0x00000400, "device supports 5GHz frequencies");

        private final long value;
        private final String message;

        private NMDeviceWifiCapabilities(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(UInt32 value) {
            return ((this.value & value.longValue()) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }
    }

    /**
     * 802.11 access point flags.
     *
     */
    enum NM80211ApFlags {
        NM_802_11_AP_FLAGS_NONE(0x00000000, "access point has no special capabilities"),
        NM_802_11_AP_FLAGS_PRIVACY(0x00000001,
                "access point requires authentication and encryption (usually means WEP)");

        private final long value;
        private final String message;

        private NM80211ApFlags(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }
    }

    /**
     * 802.11 access point security and authentication flags. These flags describe the current security requirements of
     * an access point as determined from the access point's beacon.
     *
     */
    enum NM80211ApSecurityFlags {
        NM_802_11_AP_SEC_NONE(0x00000000, "the access point has no special security requirements"),
        NM_802_11_AP_SEC_PAIR_WEP40(0x00000001, "40/64-bit WEP is supported for pairwise/unicast encryption"),
        NM_802_11_AP_SEC_PAIR_WEP104(0x00000002, "104/128-bit WEP is supported for pairwise/unicast encryption"),
        NM_802_11_AP_SEC_PAIR_TKIP(0x00000004, "TKIP is supported for pairwise/unicast encryption"),
        NM_802_11_AP_SEC_PAIR_CCMP(0x00000008, "AES/CCMP is supported for pairwise/unicast encryption"),
        NM_802_11_AP_SEC_GROUP_WEP40(0x00000010, "40/64-bit WEP is supported for group/broadcast encryption"),
        NM_802_11_AP_SEC_GROUP_WEP104(0x00000020, "104/128-bit WEP is supported for group/broadcast encryption"),
        NM_802_11_AP_SEC_GROUP_TKIP(0x00000040, "TKIP is supported for group/broadcast encryption"),
        NM_802_11_AP_SEC_GROUP_CCMP(0x00000080, "AES/CCMP is supported for group/broadcast encryption"),
        NM_802_11_AP_SEC_KEY_MGMT_PSK(0x00000100, "WPA/RSN Pre-Shared Key encryption is supported"),
        NM_802_11_AP_SEC_KEY_MGMT_802_1X(0x00000200, "802.1x authentication and key management is supported");

        private final long value;
        private final String message;

        private NM80211ApSecurityFlags(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * Indicates the 802.11 mode an access point or device is currently in.
     *
     */
    enum NM80211Mode {
        NM_802_11_MODE_UNKNOWN(0, "the device or access point mode is unknown"),
        NM_802_11_MODE_ADHOC(1,
                "for both devices and access point objects, indicates the object is part of an Ad-Hoc 802.11 network without a central coordinating access point."),
        NM_802_11_MODE_INFRA(2,
                "the device or access point is in infrastructure mode. For devices, this indicates the device is an 802.11 client/station. For access point objects, this indicates the object is an access point that provides connectivity to clients."),
        NM_802_11_MODE_AP(3,
                "the device is an access point/hotspot. Not valid for access point objects; used only for hotspot mode on the local machine.");

        private final int value;
        private final String message;

        private NM80211Mode(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }
    }

    /**
     * NMBluetoothCapabilities�values indicate the usable capabilities of a Bluetooth device.
     *
     */
    enum NMBluetoothCapabilities {
        NM_BT_CAPABILITY_NONE(0x00000000, "device has no usable capabilities"),
        NM_BT_CAPABILITY_DUN(0x00000001, "device provides Dial-Up Networking capability"),
        NM_BT_CAPABILITY_NAP(0x00000002, "device provides Network Access Point capability");

        private final long value;
        private final String message;

        private NMBluetoothCapabilities(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * NMDeviceModemCapabilities�values indicate the generic radio access technology families a modem device supports.
     * For more information on the specific access technologies the device supports use the ModemManager D-Bus API.
     *
     */
    enum NMDeviceModemCapabilities {
        NM_DEVICE_MODEM_CAPABILITY_NONE(0x00000000, "modem has no usable capabilities"),
        NM_DEVICE_MODEM_CAPABILITY_POTS(0x00000001,
                "modem uses the analog wired telephone network and is not a wireless/cellular device"),
        NM_DEVICE_MODEM_CAPABILITY_CDMA_EVDO(0x00000002,
                "modem supports at least one of CDMA 1xRTT, EVDO revision 0, EVDO revision A, or EVDO revision B"),
        NM_DEVICE_MODEM_CAPABILITY_GSM_UMTS(0x00000004,
                "modem supports at least one of GSM, GPRS, EDGE, UMTS, HSDPA, HSUPA, or HSPA+ packet switched data capability"),
        NM_DEVICE_MODEM_CAPABILITY_LTE(0x00000008, "modem has LTE data capability");
        private final long value;
        private final String message;

        private NMDeviceModemCapabilities(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * WiMAX network type.
     *
     */
    enum NMWimaxNspNetworkType {
        NM_WIMAX_NSP_NETWORK_TYPE_UNKNOWN(0, "unknown network type"),
        NM_WIMAX_NSP_NETWORK_TYPE_HOME(1, "home network"),
        NM_WIMAX_NSP_NETWORK_TYPE_PARTNER(2, "partner network"),
        NM_WIMAX_NSP_NETWORK_TYPE_ROAMING_PARTNER(3, "roaming partner network");

        private final int value;
        private final String message;

        private NMWimaxNspNetworkType(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }

    }

    /**
     * Device state
     *
     */
    enum NMDeviceState {
        NM_DEVICE_STATE_UNKNOWN(0, "the device's state is unknown"),
        NM_DEVICE_STATE_UNMANAGED(10, "the device is recognized, but not managed by NetworkManager"),
        NM_DEVICE_STATE_UNAVAILABLE(20,
                "the device is managed by NetworkManager, but is not available for use. Reasons may include the wireless switched off, missing firmware, no ethernet carrier, missing supplicant or modem manager, etc."),
        NM_DEVICE_STATE_DISCONNECTED(30,
                "the device can be activated, but is currently idle and not connected to a network."),
        NM_DEVICE_STATE_PREPARE(40,
                "the device is preparing the connection to the network. This may include operations like changing the MAC address, setting physical link properties, and anything else required to connect to the requested network."),
        NM_DEVICE_STATE_CONFIG(50,
                "the device is connecting to the requested network. This may include operations like associating with the WiFi AP, dialing the modem, connecting to the remote Bluetooth device, etc."),
        NM_DEVICE_STATE_NEED_AUTH(60,
                "the device requires more information to continue connecting to the requested network. This includes secrets like WiFi passphrases, login passwords, PIN codes, etc."),
        NM_DEVICE_STATE_IP_CONFIG(70,
                "the device is requesting IPv4 and/or IPv6 addresses and routing information from the network."),
        NM_DEVICE_STATE_IP_CHECK(80,
                "the device is checking whether further action is required for the requested network connection. This may include checking whether only local network access is available, whether a captive portal is blocking access to the Internet, etc."),
        NM_DEVICE_STATE_SECONDARIES(90,
                "the device is waiting for a secondary connection (like a VPN) which must activated before the device can be activated"),
        NM_DEVICE_STATE_ACTIVATED(100, "the device has a network connection, either local or global."),
        NM_DEVICE_STATE_DEACTIVATING(110,
                "a disconnection from the current network connection was requested, and the device is cleaning up resources used for that connection. The network connection may still be valid."),
        NM_DEVICE_STATE_FAILED(120,
                "the device failed to connect to the requested network and is cleaning up the connection request");

        private final int value;
        private final String message;

        private NMDeviceState(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }

    }

    /**
     * Device state change reason codes
     *
     */
    enum NMDeviceStateReason {
        NM_DEVICE_STATE_REASON_NONE(0, "No reason given"),
        NM_DEVICE_STATE_REASON_UNKNOWN(1, "Unknown error"),
        NM_DEVICE_STATE_REASON_NOW_MANAGED(2, "Device is now managed"),
        NM_DEVICE_STATE_REASON_NOW_UNMANAGED(3, "Device is now unmanaged"),
        NM_DEVICE_STATE_REASON_CONFIG_FAILED(4, "The device could not be readied for configuration"),
        NM_DEVICE_STATE_REASON_IP_CONFIG_UNAVAILABLE(5,
                "IP configuration could not be reserved (no available address, timeout, etc)"),
        NM_DEVICE_STATE_REASON_IP_CONFIG_EXPIRED(6, "The IP config is no longer valid"),
        NM_DEVICE_STATE_REASON_NO_SECRETS(7, "Secrets were required, but not provided"),
        NM_DEVICE_STATE_REASON_SUPPLICANT_DISCONNECT(8, "802.1x supplicant disconnected"),
        NM_DEVICE_STATE_REASON_SUPPLICANT_CONFIG_FAILED(9, "802.1x supplicant configuration failed"),
        NM_DEVICE_STATE_REASON_SUPPLICANT_FAILED(10, "802.1x supplicant failed"),
        NM_DEVICE_STATE_REASON_SUPPLICANT_TIMEOUT(11, "802.1x supplicant took too long to authenticate"),
        NM_DEVICE_STATE_REASON_PPP_START_FAILED(12, "PPP service failed to start"),
        NM_DEVICE_STATE_REASON_PPP_DISCONNECT(13, "PPP service disconnected"),
        NM_DEVICE_STATE_REASON_PPP_FAILED(14, "PPP failed"),
        NM_DEVICE_STATE_REASON_DHCP_START_FAILED(15, "DHCP client failed to start"),
        NM_DEVICE_STATE_REASON_DHCP_ERROR(16, "DHCP client error"),
        NM_DEVICE_STATE_REASON_DHCP_FAILED(17, "DHCP client failed"),
        NM_DEVICE_STATE_REASON_SHARED_START_FAILED(18, "Shared connection service failed to start"),
        NM_DEVICE_STATE_REASON_SHARED_FAILED(19, "Shared connection service failed"),
        NM_DEVICE_STATE_REASON_AUTOIP_START_FAILED(20, "AutoIP service failed to start"),
        NM_DEVICE_STATE_REASON_AUTOIP_ERROR(21, "AutoIP service error"),
        NM_DEVICE_STATE_REASON_AUTOIP_FAILED(22, "AutoIP service failed"),
        NM_DEVICE_STATE_REASON_MODEM_BUSY(23, "The line is busy"),
        NM_DEVICE_STATE_REASON_MODEM_NO_DIAL_TONE(24, "No dial tone"),
        NM_DEVICE_STATE_REASON_MODEM_NO_CARRIER(25, "No carrier could be established"),
        NM_DEVICE_STATE_REASON_MODEM_DIAL_TIMEOUT(26, "The dialing request timed out"),
        NM_DEVICE_STATE_REASON_MODEM_DIAL_FAILED(27, "The dialing attempt failed"),
        NM_DEVICE_STATE_REASON_MODEM_INIT_FAILED(28, "Modem initialization failed"),
        NM_DEVICE_STATE_REASON_GSM_APN_FAILED(29, "Failed to select the specified APN"),
        NM_DEVICE_STATE_REASON_GSM_REGISTRATION_NOT_SEARCHING(30, "Not searching for networks"),
        NM_DEVICE_STATE_REASON_GSM_REGISTRATION_DENIED(31, "Network registration denied"),
        NM_DEVICE_STATE_REASON_GSM_REGISTRATION_TIMEOUT(32, "Network registration timed out"),
        NM_DEVICE_STATE_REASON_GSM_REGISTRATION_FAILED(33, "Failed to register with the requested network"),
        NM_DEVICE_STATE_REASON_GSM_PIN_CHECK_FAILED(34, "PIN check failed"),
        NM_DEVICE_STATE_REASON_FIRMWARE_MISSING(35, "Necessary firmware for the device may be missing"),
        NM_DEVICE_STATE_REASON_REMOVED(36, "The device was removed"),
        NM_DEVICE_STATE_REASON_SLEEPING(37, "NetworkManager went to sleep"),
        NM_DEVICE_STATE_REASON_CONNECTION_REMOVED(38, "The device's active connection disappeared"),
        NM_DEVICE_STATE_REASON_USER_REQUESTED(39, "Device disconnected by user or client"),
        NM_DEVICE_STATE_REASON_CARRIER(40, "Carrier/link changed"),
        NM_DEVICE_STATE_REASON_CONNECTION_ASSUMED(41, "The device's existing connection was assumed"),
        NM_DEVICE_STATE_REASON_SUPPLICANT_AVAILABLE(42, "The supplicant is now available"),
        NM_DEVICE_STATE_REASON_MODEM_NOT_FOUND(43, "The modem could not be found"),
        NM_DEVICE_STATE_REASON_BT_FAILED(44, "The Bluetooth connection failed or timed out"),
        NM_DEVICE_STATE_REASON_GSM_SIM_NOT_INSERTED(45, "GSM Modem's SIM Card not inserted"),
        NM_DEVICE_STATE_REASON_GSM_SIM_PIN_REQUIRED(46, "GSM Modem's SIM Pin required"),
        NM_DEVICE_STATE_REASON_GSM_SIM_PUK_REQUIRED(47, "GSM Modem's SIM Puk required"),
        NM_DEVICE_STATE_REASON_GSM_SIM_WRONG(48, "GSM Modem's SIM wrong"),
        NM_DEVICE_STATE_REASON_INFINIBAND_MODE(49, "InfiniBand device does not support connected mode"),
        NM_DEVICE_STATE_REASON_DEPENDENCY_FAILED(50, "A dependency of the connection failed"),
        NM_DEVICE_STATE_REASON_BR2684_FAILED(51, "Problem with the RFC 2684 Ethernet over ADSL bridge"),
        NM_DEVICE_STATE_REASON_MODEM_MANAGER_UNAVAILABLE(52, "ModemManager not running"),
        NM_DEVICE_STATE_REASON_SSID_NOT_FOUND(53, "The WiFi network could not be found"),
        NM_DEVICE_STATE_REASON_SECONDARY_CONNECTION_FAILED(54, "A secondary connection of the base connection failed"),
        NM_DEVICE_STATE_REASON_DCB_FCOE_FAILED(55, "DCB or FCoE setup failed"),
        NM_DEVICE_STATE_REASON_TEAMD_CONTROL_FAILED(56, "teamd control failed"),
        NM_DEVICE_STATE_REASON_MODEM_FAILED(57, "Modem failed or no longer available"),
        NM_DEVICE_STATE_REASON_MODEM_AVAILABLE(58, "Modem now ready and available"),
        NM_DEVICE_STATE_REASON_SIM_PIN_INCORRECT(59, "SIM PIN was incorrect"),
        NM_DEVICE_STATE_REASON_NEW_ACTIVATION(60, "New connection activation was enqueued"),
        NM_DEVICE_STATE_REASON_PARENT_CHANGED(61, "the device's parent changed"),
        NM_DEVICE_STATE_REASON_PARENT_MANAGED_CHANGED(62, "the device parent's management changed");

        private final int value;
        private final String message;

        private NMDeviceStateReason(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }
    }

    /**
     * Since: 1.2
     *
     */
    enum NMMetered {
        NM_METERED_UNKNOWN(0, "The metered status is unknown"),
        NM_METERED_YES(1, "Metered, the value was statically set"),
        NM_METERED_NO(2, "Not metered, the value was statically set"),
        NM_METERED_GUESS_YES(3, "Metered, the value was guessed"),
        NM_METERED_GUESS_NO(4, "Not metered, the value was guessed");

        private final int value;
        private final String message;

        private NMMetered(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }
    }

    /**
     * NMActiveConnectionState�values indicate the state of a connection to a specific network while it is starting,
     * connected, or disconnecting from that network.
     *
     */
    enum NMActiveConnectionState {
        NM_ACTIVE_CONNECTION_STATE_UNKNOWN(0, "the state of the connection is unknown"),
        NM_ACTIVE_CONNECTION_STATE_ACTIVATING(1, "a network connection is being prepared"),
        NM_ACTIVE_CONNECTION_STATE_ACTIVATED(2, "there is a connection to the network"),
        NM_ACTIVE_CONNECTION_STATE_DEACTIVATING(3, "the network connection is being torn down and cleaned up"),
        NM_ACTIVE_CONNECTION_STATE_DEACTIVATED(4, "the network connection is disconnected and will be removed");

        private final int value;
        private final String message;

        private NMActiveConnectionState(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }
    }

    /**
     * NMSecretAgentGetSecretsFlags�values modify the behavior of a GetSecrets request.
     *
     */
    enum NMSecretAgentGetSecretsFlags {
        NM_SECRET_AGENT_GET_SECRETS_FLAG_NONE(0x0,
                "no special behavior; by default no user interaction is allowed and requests for secrets are fulfilled from persistent storage, or if no secrets are available an error is returned."),
        NM_SECRET_AGENT_GET_SECRETS_FLAG_ALLOW_INTERACTION(0x1,
                "allows the request to interact with the user, possibly prompting via UI for secrets if any are required, or if none are found in persistent storage."),
        NM_SECRET_AGENT_GET_SECRETS_FLAG_REQUEST_NEW(0x2,
                "explicitly prompt for new secrets from the user. This flag signals that NetworkManager thinks any existing secrets are invalid or wrong. This flag implies that interaction is allowed."),
        NM_SECRET_AGENT_GET_SECRETS_FLAG_USER_REQUESTED(0x4,
                "set if the request was initiated by user-requested action via the D-Bus interface, as opposed to automatically initiated by NetworkManager in response to (for example) scan results or carrier changes."),
        NM_SECRET_AGENT_GET_SECRETS_FLAG_ONLY_SYSTEM(0x80000000, "Internal flag, not part of the D-Bus API."),
        NM_SECRET_AGENT_GET_SECRETS_FLAG_NO_ERRORS(0x40000000, "Internal flag, not part of the D-Bus API.");

        private final long value;
        private final String message;

        private NMSecretAgentGetSecretsFlags(final long value, final String message) {
            this.value = value;
            this.message = message;
        }

        public long get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * NMSecretAgentCapabilities�indicate various capabilities of the agent.
     *
     */
    enum NMSecretAgentCapabilities {
        NM_SECRET_AGENT_CAPABILITY_NONE(0x0, "the agent supports no special capabilities"),
        NM_SECRET_AGENT_CAPABILITY_VPN_HINTS(0x1,
                "the agent supports passing hints to VPN plugin authentication dialogs.");

        private final int value;
        private final String message;

        private NMSecretAgentCapabilities(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSet(long value) {
            return ((this.value & value) != 0);
        }

        public long set(long value, NMDeviceWifiCapabilities flag) {
            return (this.value | value);
        }

        public long unSet(long value, NMDeviceWifiCapabilities flag) {
            return (value & (0xFFFFFFFF ^ flag.get()));
        }

    }

    /**
     * The tunneling mode.
     *
     */
    enum NMIPTunnelMode {
        NM_IP_TUNNEL_MODE_UNKNOWN(0, "Unknown/unset tunnel mode"),
        NM_IP_TUNNEL_MODE_IPIP(1, "IP in IP tunnel"),
        NM_IP_TUNNEL_MODE_GRE(2, "GRE tunnel"),
        NM_IP_TUNNEL_MODE_SIT(3, "SIT tunnel"),
        NM_IP_TUNNEL_MODE_ISATAP(4, "ISATAP tunnel"),
        NM_IP_TUNNEL_MODE_VTI(5, "VTI tunnel"),
        NM_IP_TUNNEL_MODE_IP6IP6(6, "IPv6 in IPv6 tunnel"),
        NM_IP_TUNNEL_MODE_IPIP6(7, "IPv4 in IPv6 tunnel"),
        NM_IP_TUNNEL_MODE_IP6GRE(8, "IPv6 GRE tunnel"),
        NM_IP_TUNNEL_MODE_VTI6(9, "IPv6 VTI tunnel");

        private final int value;
        private final String message;

        private NMIPTunnelMode(final int value, final String message) {
            this.value = value;
            this.message = message;
        }

        public int get() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean equals(int value) {
            return this.value == value;
        }
    }
}
