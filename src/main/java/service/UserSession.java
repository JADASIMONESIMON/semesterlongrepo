package service;

import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance;

    private String userName;
    private String password;
    private String privileges;

    private final Object lock = new Object();

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        synchronized (lock) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put("USERNAME", userName);
            userPreferences.put("PASSWORD", password);
            userPreferences.put("PRIVILEGES", privileges);
        }
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(userName, password, privileges);
                }
            }
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(userName, password, "NONE");
                }
            }
        }
        return instance;
    }

    public synchronized String getUserName() {
        return this.userName;
    }

    public synchronized String getPassword() {
        return this.password;
    }

    public synchronized String getPrivileges() {
        return this.privileges;
    }

    public synchronized void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";
        synchronized (lock) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.remove("USERNAME");
            userPreferences.remove("PASSWORD");
            userPreferences.remove("PRIVILEGES");
        }
    }

    @Override
    public synchronized String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                ", privileges='" + privileges + '\'' +
                '}';
    }
}