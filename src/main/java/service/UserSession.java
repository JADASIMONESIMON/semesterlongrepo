package service;

import java.util.prefs.Preferences;

public class UserSession {
    private static volatile UserSession instance;

    private final String userName;
    private final String password;

    private static final Object LOCK = new Object();
    private static final String PREF_USERNAME = "USERNAME";
    private static final String PREF_PASSWORD = "PASSWORD";

    private UserSession(String userName, String password) {
        this.userName = userName;
        this.password = password;
        saveToPreferences();
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
            String storedUsername = userPreferences.get(PREF_USERNAME, null);
            String storedPassword = userPreferences.get(PREF_PASSWORD, null);
            if (storedUsername != null && storedPassword != null) {
                return new UserSession(storedUsername, storedPassword);
            }
            return null;
        }
    }

    private void saveToPreferences() {
        synchronized (LOCK) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.put(PREF_USERNAME, userName);
            userPreferences.put(PREF_PASSWORD, password);
        }
    }

    public void clearSession() {
        synchronized (LOCK) {
            Preferences userPreferences = Preferences.userRoot();
            userPreferences.remove(PREF_USERNAME);
            userPreferences.remove(PREF_PASSWORD);
            instance = null;
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
