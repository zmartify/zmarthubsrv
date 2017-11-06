package org.freedesktop;

import java.util.Map;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.UInt64;

public final class User extends Struct {
    @Position(0)
    public final UInt64 uid;
    @Position(1)
    public final String userName;
    @Position(2)
    public final String realName;
    @Position(3)
    public final int accountType;
    @Position(4)
    public final String homeDirectory;
    @Position(5)
    public final String shell;
    @Position(6)
    public final String email;
    @Position(7)
    public final String language;
    @Position(8)
    public final String formatsLocale;
    @Position(9)
    public final Map<String, String> inputSources;
    @Position(10)
    public final String xSession;
    @Position(11)
    public final String location;
    @Position(12)
    public final UInt64 loginFrequency;
    @Position(13)
    public final LoginHistory loginHistory;
    @Position(14)
    public final boolean xHasMessages;
    @Position(15)
    public final String[] xKeyboardsLayouts;
    @Position(16)
    public final String backgroundFile;
    @Position(17)
    public final String iconFile;
    @Position(18)
    public final boolean Locked;
    @Position(19)
    public final int passwordMode;
    @Position(20)
    public final String passwordHint;
    @Position(21)
    public final boolean automaticLogin;
    @Position(22)
    public final boolean systemAccount;
    @Position(23)
    public final boolean localAccount;

    public User(UInt64 uid, String userName, String realName, int accountType, String homeDirectory, String shell,
            String email, String language, String formatsLocale, Map<String, String> inputSources, String xSession,
            String location, UInt64 loginFrequency, LoginHistory loginHistory, boolean xHasMessages,
            String[] xKeyboardsLayouts, String backgroundFile, String iconFile, boolean locked, int passwordMode,
            String passwordHint, boolean automaticLogin, boolean systemAccount, boolean localAccount) {
        super();
        this.uid = uid;
        this.userName = userName;
        this.realName = realName;
        this.accountType = accountType;
        this.homeDirectory = homeDirectory;
        this.shell = shell;
        this.email = email;
        this.language = language;
        this.formatsLocale = formatsLocale;
        this.inputSources = inputSources;
        this.xSession = xSession;
        this.location = location;
        this.loginFrequency = loginFrequency;
        this.loginHistory = loginHistory;
        this.xHasMessages = xHasMessages;
        this.xKeyboardsLayouts = xKeyboardsLayouts;
        this.backgroundFile = backgroundFile;
        this.iconFile = iconFile;
        Locked = locked;
        this.passwordMode = passwordMode;
        this.passwordHint = passwordHint;
        this.automaticLogin = automaticLogin;
        this.systemAccount = systemAccount;
        this.localAccount = localAccount;
    }

}
