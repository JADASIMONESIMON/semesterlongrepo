package viewmodel;

import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final MyLogger logger = new MyLogger();

    @FXML
    private Button addButton, clearButton, deleteButton, editButton;

    @FXML
    private Label statusLabel;

    @FXML
    private MenuItem importCsvItem, exportCsvItem, deleteItem;

    @FXML
    private TableView<Person> tv;

    @FXML
    private TableColumn<Person, Integer> tv_id;

    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    @FXML
    private TextField first_name, last_name, department, email;

    @FXML
    private ComboBox<String> majorDropdown;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            if (!cnUtil.testConnection()) {
                showStatusMessage("Database connection failed!", 5);
                logger.makeLog("Database connection failed during initialization.");
                return;
            }

            // Initialize table columns
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));

            // Load initial data
            loadData();

            // Initialize major dropdown with enum values
            majorDropdown.setItems(FXCollections.observableArrayList("CS", "CPIS", "English"));

            // Bind buttons and menu items to TableView selection
            editButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            // Disable add button until form is valid
            BooleanBinding formValidBinding = createFormValidationBinding(
                    first_name.textProperty(),
                    last_name.textProperty(),
                    department.textProperty(),
                    majorDropdown.valueProperty(),
                    email.textProperty()
            );
            addButton.disableProperty().bind(formValidBinding);
        } catch (Exception e) {
            logger.makeLog("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void showImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                // Process selected image file
                System.out.println("Image selected: " + selectedFile.getAbsolutePath());
            } else {
                System.out.println("Image selection cancelled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadData() {
        try {
            ObservableList<Person> data = cnUtil.getData();
            tv.setItems(data);
            logger.makeLog("Data loaded successfully.");
        } catch (Exception e) {
            logger.makeLog("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BooleanBinding createFormValidationBinding(
            StringProperty firstName,
            StringProperty lastName,
            StringProperty department,
            ObjectProperty<String> major,
            StringProperty email
    ) {
        return Bindings.createBooleanBinding(
                () -> firstName.get().trim().isEmpty()
                        || lastName.get().trim().isEmpty()
                        || department.get().trim().isEmpty()
                        || major.get() == null
                        || email.get().trim().isEmpty()
                        || !isEmailValid(email.get()),
                firstName, lastName, department, email
        );
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$";
        return email.matches(emailRegex);
    }

    @FXML
    protected void addNewRecord() {
        try {
            Person p = new Person(
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorDropdown.getValue(),
                    email.getText(),
                    null
            );
            cnUtil.insertUser(p);
            tv.getItems().add(p);
            clearForm();
            showStatusMessage("Record added successfully!", 5);
        } catch (Exception e) {
            showStatusMessage("Failed to add record.", 5);
            logger.makeLog("Error adding record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void deleteRecord() {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            if (p != null) {
                cnUtil.deleteUser(p.getId());
                tv.getItems().remove(p);
                showStatusMessage("Record deleted successfully!", 5);
            }
        } catch (Exception e) {
            showStatusMessage("Failed to delete record.", 5);
            logger.makeLog("Error deleting record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void logOut() {
        try {
            // Load the login screen
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) tv.getScene().getWindow(); // Get current stage
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during logout: " + e.getMessage());
        }
    }
    @FXML
    public void closeApplication(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    protected void importCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        File file = fileChooser.showOpenDialog(tv.getScene().getWindow());
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    Person p = new Person(fields[1], fields[2], fields[3], fields[4], fields[5], null);
                    cnUtil.insertUser(p);
                    tv.getItems().add(p);
                }
                showStatusMessage("CSV imported successfully!", 5);
            } catch (IOException e) {
                showStatusMessage("Failed to import CSV.", 5);
                logger.makeLog("Error importing CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void exportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        File file = fileChooser.showSaveDialog(tv.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (Person p : tv.getItems()) {
                    pw.println(String.format("%d,%s,%s,%s,%s,%s",
                            p.getId(), p.getFirstName(), p.getLastName(), p.getDepartment(), p.getMajor(), p.getEmail()));
                }
                showStatusMessage("CSV exported successfully!", 5);
            } catch (IOException e) {
                showStatusMessage("Failed to export CSV.", 5);
                logger.makeLog("Error exporting CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorDropdown.setValue(null);
        email.clear();
        showStatusMessage("Form cleared.", 3);
    }

    private void showStatusMessage(String message, int duration) {
        statusLabel.setText(message);
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(e -> statusLabel.setText(""));
        pause.play();
    }

    public void lightTheme(ActionEvent actionEvent) {
    }

    public void darkTheme(ActionEvent actionEvent) {
    }

    public void displayAbout(ActionEvent actionEvent) {
    }

    public void editRecord(ActionEvent actionEvent) {
    }

    public void selectedItemTV(MouseEvent mouseEvent) {
    }
}
