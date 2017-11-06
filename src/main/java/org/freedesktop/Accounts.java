package org.freedesktop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import org.freedesktop.DBus.GLib.CSymbol;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

public interface Accounts extends DBusInterface {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface VendorExtension {
        String value();
    }

    public interface IUser extends DBusInterface {

        public static class Changed extends DBusSignal {
            public Changed(String path) throws DBusException {
                super(path);
            }
        }

        public void SetUserName(String name);

        public void SetRealName(String name);

        public void SetEmail(String email);

        public void SetLanguage(String language);

        public void SetFormatsLocale(String formats_locale);

        public void SetInputSources(List<Map<String, String>> sources);

        @CSymbol("user_set_x_session")
        public void SetXSession(String x_session);

        public void SetLocation(String location);

        public void SetHomeDirectory(String homedir);

        public void SetShell(String shell);

        public void SetXHasMessages(boolean has_messages);

        public void SetXKeyboardLayouts(List<String> layouts);

        public void SetBackgroundFile(String filename);

        public void SetIconFile(String filename);

        public void SetLocked(boolean locked);

        public void SetAccountType(int accountType);

        public void SetPasswordMode(int mode);

        public void SetPassword(String password, String hint);

        public void SetAutomaticLogin(boolean enabled);

    }

    public interface Authentication extends DBusInterface {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface ChangeAny {
            String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface ChangeOwn {
            String value();
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface ReadAny {
            String value();
        }
    }

    public static class UserAdded extends DBusSignal {
        public final DBusInterface user;

        public UserAdded(String path, DBusInterface user) throws DBusException {
            super(path, user);
            this.user = user;
        }
    }

    public static class UserDeleted extends DBusSignal {
        public final DBusInterface user;

        public UserDeleted(String path, DBusInterface user) throws DBusException {
            super(path, user);
            this.user = user;
        }
    }

    public List<DBusInterface> ListCachedUsers();

    public User FindUserById(long id);

    public User FindUserByName(String name);

    public DBusInterface CreateUser(String name, String fullname, int accountType);

    public DBusInterface CacheUser(String name);

    public void UncacheUser(String name);

    public void DeleteUser(long id, boolean removeFiles);

}
