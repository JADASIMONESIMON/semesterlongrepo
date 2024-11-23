package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance;

    private final String userName;
    private final String password;

    private final static Object LOCK = new Object();

    private UserSession(String userName, String password) {
        this.userName = userName;
        this.password = password;
        synchronized (LOCK) {
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
        synchronized (LOCK) {
            Preferences userPreferences = Preferences.userRoot();
            String storedUsername = userPreferences.get("USERNAME", null);
            String storedPassword = userPreferences.get("PASSWORD", null);
            if (storedUsername != null && storedPassword != null) {
                return getInstance(storedUsername, storedPassword);
            }
            return null;
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void cleanUserSession() {
        synchronized (LOCK) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.remove("USERNAME");
            userPreferences.remove("PASSWORD");
        }
        synchronized (this) {
            instance = null; // Clear the singleton instance for fresh initialization
        }
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
