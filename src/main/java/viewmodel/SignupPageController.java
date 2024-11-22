package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.UserSession;
import service.MyLogger;

public class SignupPageController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Handles the signup button click event.
     */
    @FXML
    public void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username and password cannot be empty.");
            MyLogger.makeLog("Signup failed: Username or password was empty.");
            return;
        }

        try {
            // Create a new user session
            UserSession session = UserSession.getInstance(username, password);

            // Log success and show confirmation
            MyLogger.makeLog("Signup successful for user: " + session.getUserName());
            showAlert(Alert.AlertType.INFORMATION, "Signup Success", "Welcome, " + username + "!");
        } catch (Exception e) {
            // Log error and show error message
            MyLogger.makeLog("Signup error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Signup Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Utility method to show alerts.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
