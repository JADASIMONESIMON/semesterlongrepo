package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
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
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
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

            // Navigate to the login page after successful signup
            navigateToLogin();

        } catch (Exception e) {
            // Log any errors
            MyLogger.makeLog("Signup error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Signup Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Handles the "Back to Login" button click.
     */
    @FXML
    public void goBack() {
        navigateToLogin();
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

    /**
     * Navigates back to the login page.
     */
    private void navigateToLogin() {
        try {
            // Load the login page FXML file
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene loginScene = new Scene(loginRoot);

            // Get the current stage
            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            // Set the login scene and show it
            currentStage.setScene(loginScene);
            currentStage.show();

            MyLogger.makeLog("Successfully navigated to the login page.");
        } catch (NullPointerException e) {
            MyLogger.makeLog("Navigation error: FXML element (usernameField) is null. Check fx:id mapping.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "UI element is not properly mapped.");
        } catch (Exception e) {
            MyLogger.makeLog("Navigation error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "An unexpected error occurred while navigating to the login page.");
        }
    }

}
