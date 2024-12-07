package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SignUpController {

    @FXML
    private TextField emailfield;


    @FXML
    private TextField usernamefield;

    @FXML
    private Label errormessagelabel;

    @FXML
    private TextField fnameField;

    @FXML
    private TextField lnamefield;

    @FXML
    private Button loginbtn;

    @FXML
    private PasswordField passwordfield;

    @FXML
    private Button submitbtn;

    private static String savedUsername;
    private static String savedPassword;

    /**
     * Validates all fields and saves the username and password if valid.
     */
    @FXML
    void handleSubmit(ActionEvent event) {
        boolean isValid = true;
        errormessagelabel.setText("");

        if (!usernamefield.getText().matches("^[a-z]+$")) { // Only lowercase letters
            highlightField(usernamefield, true);
            errormessagelabel.setText("Username must contain only lowercase letters.");
            isValid = false;
        } else {
            highlightField(usernamefield, false);
        }

        // Validate first name
        if (!fnameField.getText().matches("^[A-Z][a-z]*$")) {
            highlightField(fnameField, true);
            errormessagelabel.setText("First name must start with a capital letter followed by lowercase letters.");
            isValid = false;
        } else {
            highlightField(fnameField, false);
        }

        // Validate last name
        if (!lnamefield.getText().matches("^[A-Z][a-z]*$")) {
            highlightField(lnamefield, true);
            errormessagelabel.setText("Last name must start with a capital letter followed by lowercase letters.");
            isValid = false;
        } else {
            highlightField(lnamefield, false);
        }

        // Validate email
        if (!emailfield.getText().matches("^[\\w._%+-]+@farmingdale\\.edu$")) {
            highlightField(emailfield, true);
            errormessagelabel.setText("Email must end with '@farmingdale.edu'.");
            isValid = false;
        } else {
            highlightField(emailfield, false);
        }

        // Validate password
        if (!passwordfield.getText().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$")) {
            highlightField(passwordfield, true);
            errormessagelabel.setText("Password must contain at least one uppercase letter, one lowercase letter, and one digit.");
            isValid = false;
        } else {
            highlightField(passwordfield, false);
        }

        if (isValid) {
            savedUsername = usernamefield.getText();
            savedPassword = passwordfield.getText();
            errormessagelabel.setTextFill(Color.GREEN);
            errormessagelabel.setText("Signup successful. Redirecting to login...");
            redirectToLogin();
        }
    }

    /**
     * Navigates back to the login page regardless of field values.
     */
    @FXML
    void handleBackToLogin(ActionEvent event) {
        redirectToLogin();
    }

    /**
     * Highlights a text field in red if invalid.
     */
    private void highlightField(TextField field, boolean isInvalid) {
        if (isInvalid) {
            field.setStyle("-fx-border-color: red;");
        } else {
            field.setStyle("-fx-border-color: transparent;");
        }
    }

    /**
     * Redirects the user to the login page.
     */
    private void redirectToLogin() {
        try {
            Stage stage = (Stage) loginbtn.getScene().getWindow();
            Scene loginScene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/login.fxml"))
            );
            stage.setScene(loginScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Static getters to retrieve saved credentials
    public static String getSavedUsername() {
        return savedUsername;
    }

    public static String getSavedPassword() {
        return savedPassword;
    }
}
