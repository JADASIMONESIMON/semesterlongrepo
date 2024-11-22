package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.UserSession;
import service.MyLogger;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button newAccountBtn;

    @FXML
    private Button goBackBtn;

    /**
     * Handles the "Create New Account" button click.
     */
    @FXML
    public void createNewAccount() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username and password cannot be empty.");
            MyLogger.makeLog("Signup failed: Username or password was empty.");
            return;
        }

        try {
            // Create a new user session and save to preferences
            UserSession session = UserSession.getInstance(username, password);

            // Log the successful creation
            MyLogger.makeLog("Signup successful for user: " + session.getUserName());
            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Welcome, " + username + "!");
        } catch (Exception e) {
            // Log any errors
            MyLogger.makeLog("Signup error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Signup Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the "Login" button click to go back to the login page.
     */
    @FXML
    public void goBack() {
        // Navigate back to the login screen
        showAlert(Alert.AlertType.INFORMATION, "Navigation", "Going back to login page...");
        MyLogger.makeLog("Navigating back to login page.");
        // You can implement navigation logic here using a scene loader
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
