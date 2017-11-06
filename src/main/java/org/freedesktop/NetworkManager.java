package org.freedesktop;

import java.util.List;
import java.util.Map;

import org.freedesktop.DBus.Deprecated;
import org.freedesktop.DBus.GLib.CSymbol;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

public interface NetworkManager extends DBusInterface {

    public interface AccessPoint extends DBusInterface {
        /*
         * @Position(0) public final byte[] Ssid;
         * 
         * @Position(1) public final byte Strength;
         * 
         * @Position(2) public final int LastSeen;
         * 
         * @Position(3) public final String HwAddress;
         * 
         * @Position(4) public final UInt32 Flags;
         * 
         * @Position(5) public final UInt32 Frequency;
         * 
         * @Position(6) public final UInt32 MaxBitrate;
         * 
         * @Position(7) public final UInt32 Mode;
         * 
         * @Position(8) public final UInt32 RsnFlags;
         * 
         * @Position(9) public final UInt32 WpaFlags;
         */
        public static class PropertiesChanged extends DBusSignal {
            public final Map<String, Variant<?>> properties;

            public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
                super(path, properties);
                this.properties = properties;
            }
        }

    }

    public interface Device extends DBusInterface {
        public interface Wireless extends DBusInterface {
            public static class PropertiesChanged extends DBusSignal {
                public final Map<String, Variant<?>> properties;

                public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
                    super(path, properties);
                    this.properties = properties;
                }
            }

            public static class AccessPointAdded extends DBusSignal {
                public final DBusInterface access_point;

                public AccessPointAdded(String path, DBusInterface access_point) throws DBusException {
                    super(path, access_point);
                    this.access_point = access_point;
                }
            }

            public static class AccessPointRemoved extends DBusSignal {
                public final DBusInterface access_point;

                public AccessPointRemoved(String path, DBusInterface access_point) throws DBusException {
                    super(path, access_point);
                    this.access_point = access_point;
                }
            }

            public static class ScanDone extends DBusSignal {
                public ScanDone(String path) throws DBusException {
                    super(path);
                }
            }

            @Deprecated
            public List<DBusInterface> GetAccessPoints();

            public List<DBusInterface> GetAllAccessPoints();

            public void RequestScan(Map<String, Variant<?>> options);

        }

        public static class StateChanged extends DBusSignal {
            public final UInt32 new_state;
            public final UInt32 old_state;
            public final UInt32 reason;

            public StateChanged(String path, UInt32 new_state, UInt32 old_state, UInt32 reason) throws DBusException {
                super(path, new_state, old_state, reason);
                this.new_state = new_state;
                this.old_state = old_state;
                this.reason = reason;
            }
        }

        public void Reapply(Map<String, Map<String, Variant<?>>> connection, UInt64 version_id, UInt32 flags);

        public Pair<Map<String, Map<String, Variant<?>>>, UInt64> GetAppliedConnection(UInt32 flags);

        public void Disconnect();

        public void Delete();

    }

    public interface Settings extends DBusInterface {
        public interface Connection extends DBusInterface {
            public static class Updated extends DBusSignal {
                public Updated(String path) throws DBusException {
                    super(path);
                }
            }

            public static class Removed extends DBusSignal {
                public Removed(String path) throws DBusException {
                    super(path);
                }
            }

            public static class PropertiesChanged extends DBusSignal {
                public final Map<String, Variant<?>> properties;

                public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
                    super(path, properties);
                    this.properties = properties;
                }
            }

            public void Update(Map<String, Map<String, Variant<?>>> properties);

            public void UpdateUnsaved(Map<String, Map<String, Variant<?>>> properties);

            public void Delete();

            public Map<String, Map<String, Variant<?>>> GetSettings();

            public Map<String, Map<String, Variant<?>>> GetSecrets(String setting_name);

            public void ClearSecrets();

            public void Save();

        }

        public static class PropertiesChanged extends DBusSignal {
            public final Map<String, Variant<?>> properties;

            public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
                super(path, properties);
                this.properties = properties;
            }
        }

        public static class NewConnection extends DBusSignal {
            public final DBusInterface connection;

            public NewConnection(String path, DBusInterface connection) throws DBusException {
                super(path, connection);
                this.connection = connection;
            }
        }

        public static class ConnectionRemoved extends DBusSignal {
            public final DBusInterface connection;

            public ConnectionRemoved(String path, DBusInterface connection) throws DBusException {
                super(path, connection);
                this.connection = connection;
            }
        }

        public List<DBusInterface> ListConnections();

        public DBusInterface GetConnectionByUuid(String uuid);

        public DBusInterface AddConnection(Map<String, Map<String, Variant<?>>> connection);

        public DBusInterface AddConnectionUnsaved(Map<String, Map<String, Variant<?>>> connection);

        public Pair<Boolean, List<String>> LoadConnections(List<String> filenames);

        public boolean ReloadConnections();

        public void SaveHostname(String hostname);

    }

    public static class CheckPermissions extends DBusSignal {
        public CheckPermissions(String path) throws DBusException {
            super(path);
        }
    }

    public static class StateChanged extends DBusSignal {
        public final UInt32 state;

        public StateChanged(String path, UInt32 state) throws DBusException {
            super(path, state);
            this.state = state;
        }
    }

    public static class PropertiesChanged extends DBusSignal {
        public final Map<String, Variant<?>> properties;

        public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
            super(path, properties);
            this.properties = properties;
        }
    }

    public static class DeviceAdded extends DBusSignal {
        public final DBusInterface device_path;

        public DeviceAdded(String path, DBusInterface device_path) throws DBusException {
            super(path, device_path);
            this.device_path = device_path;
        }
    }

    public static class DeviceRemoved extends DBusSignal {
        public final DBusInterface device_path;

        public DeviceRemoved(String path, DBusInterface device_path) throws DBusException {
            super(path, device_path);
            this.device_path = device_path;
        }
    }

    public void Reload(UInt32 flags);

    public List<DBusInterface> GetDevices();

    @CSymbol("impl_manager_get_all_devices")
    public List<DBusInterface> GetAllDevices();

    public DBusInterface GetDeviceByIpIface(String iface);

    public DBusInterface ActivateConnection(DBusInterface connection, DBusInterface device,
            DBusInterface specific_object);

    public Pair<DBusInterface, DBusInterface> AddAndActivateConnection(Map<String, Map<String, Variant<?>>> connection,
            DBusInterface device, DBusInterface specific_object);

    public void DeactivateConnection(DBusInterface active_connection);

    public void Sleep(boolean sleep);

    public void Enable(boolean enable);

    public Map<String, String> GetPermissions();

    public void SetLogging(String level, String domains);

    public Pair<String, String> GetLogging();

    public UInt32 CheckConnectivity();

    public UInt32 state();

}
