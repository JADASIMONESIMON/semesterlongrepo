package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance;

    private String userName;
    private String password;

    private final Object lock = new Object();

    private UserSession(String userName, String password) {
        this.userName = userName;
        this.password = password;
        synchronized (lock) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put("USERNAME", userName);
            userPreferences.put("PASSWORD", password);
        }
    }

    public static UserSession getInstance(String userName, String password) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(userName, password);
                }
            }
        }
        return instance;
    }

    public static UserSession loadFromPreferences() {
        synchronized (UserSession.class) {
            Preferences userPreferences = Preferences.userRoot();
            String storedUsername = userPreferences.get("USERNAME", "");
            String storedPassword = userPreferences.get("PASSWORD", "");
            if (!storedUsername.isEmpty() && !storedPassword.isEmpty()) {
                return getInstance(storedUsername, storedPassword);
            }
            return null;
        }
    }

    public synchronized String getUserName() {
        return this.userName;
    }

    public synchronized String getPassword() {
        return this.password;
    }

    public synchronized void cleanUserSession() {
        this.userName = "";
        this.password = "";
        synchronized (lock) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.remove("USERNAME");
            userPreferences.remove("PASSWORD");
        }
    }

    @Override
    public synchronized String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
