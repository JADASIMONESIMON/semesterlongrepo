package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button loginBtn;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button signUpButton;

    /**
     * Handles the login button action.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String enteredUsername = usernameTextField.getText();
        String enteredPassword = passwordField.getText();

        if (authenticate(enteredUsername, enteredPassword)) {
            redirectToGUI();
        } else {
            showAlert("Invalid Login", "Username or password is incorrect. Please try again.");
        }
    }

    /**
     * Handles the sign-up button action.
     */
    @FXML
    void handleSignUp(ActionEvent event) {
        redirectToSignUp();
    }

    /**
     * Authenticates the entered username and password against saved credentials.
     */
    private boolean authenticate(String username, String password) {
        return username.equals(SignUpController.getSavedUsername()) &&
                password.equals(SignUpController.getSavedPassword());
    }

    /**
     * Redirects to the main GUI.
     */
    private void redirectToGUI() {
        try {
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            Scene guiScene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"))
            );
            stage.setScene(guiScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Redirects to the signup page.
     */
    private void redirectToSignUp() {
        try {
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            Scene signUpScene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/signup.fxml"))
            );
            stage.setScene(signUpScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert with the given title and message.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
