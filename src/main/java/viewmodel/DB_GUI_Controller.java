package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import model.Person;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();

    @FXML
    private Button addButton, clearButton, deleteButton, editButton;

    @FXML
    private Label statusLabel;

    @FXML
    private MenuItem deleteItem;

    @FXML
    private VBox progressBarContainer;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField first_name, last_name, department, email, imageURL;

    @FXML
    private ComboBox<String> majorDropdown;

    @FXML
    private ImageView img_view;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TableView<Person> tv;

    @FXML
    private TableColumn<Person, Integer> tv_id;

    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = FXCollections.observableArrayList();



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            data.addAll(cnUtil.getData());

            // Initialize dropdown
            for (Major major : Major.values()) {
                majorDropdown.getItems().add(major.getDisplayName());
            }

            // Bind disableProperty of buttons to the TableView selection model
            editButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            // Bind delete menu item
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            // Disable add button until all fields are valid
            BooleanBinding formValidBinding = createFormValidationBinding(
                    first_name.textProperty(),
                    last_name.textProperty(),
                    department.textProperty(),
                    majorDropdown.valueProperty(),
                    email.textProperty()
            );
            addButton.disableProperty().bind(formValidBinding);

        } catch (Exception e) {
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
                        || majorDropdown.getValue() == null
                        || email.get().trim().isEmpty()
                        || !isDepartmentValid(department.get())
                        || !isEmailValid(email.get()),
                firstName, lastName, department, email
        );
    }

    private boolean isDepartmentValid(String department) {
        return department != null && department.matches("[a-zA-Z ]+");
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && email.matches(emailRegex);
    }

    @FXML
    protected void addNewRecord() {
        Person p = new Person(
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                Major.fromString(majorDropdown.getValue()).name(),
                email.getText(),
                imageURL.getText()
        );
        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorDropdown.setValue(null);
        email.clear();
        imageURL.clear();
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            cnUtil.deleteRecord(p);
            data.remove(p);
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            int index = data.indexOf(p);
            Person updatedPerson = new Person(
                    p.getId(),
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    Major.fromString(majorDropdown.getValue()).name(),
                    email.getText(),
                    imageURL.getText()
            );
            cnUtil.editUser(p.getId(), updatedPerson);
            data.set(index, updatedPerson);
        }
    }

    @FXML
    protected void showImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            uploadFile(file);
        }
    }

    private void uploadFile(File file) {
        Task<Void> uploadTask = createUploadTask(file);
        progressBar.progressProperty().bind(uploadTask.progressProperty());
        new Thread(uploadTask).start();
    }

    private Task<Void> createUploadTask(File file) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream outputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long uploadedBytes = 0;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;
                        updateProgress(uploadedBytes, fileSize);
                    }
                }
                return null;
            }
        };
    }



    @FXML
    public void selectedItemTV(MouseEvent mouseEvent) {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            first_name.setText(selectedPerson.getFirstName());
            last_name.setText(selectedPerson.getLastName());
            department.setText(selectedPerson.getDepartment());
            majorDropdown.setValue(selectedPerson.getMajor());
            email.setText(selectedPerson.getEmail());
            imageURL.setText(selectedPerson.getImageURL());
        }
    }

    @FXML
    protected void displayAbout(ActionEvent event) {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About This Application");
        aboutAlert.setHeaderText("CSC311 Database Project");
        aboutAlert.setContentText(
                "This application was developed as part of the CSC311 course project.\n\n" +
                        "Features include:\n" +
                        "- User management\n" +
                        "- Azure Blob Storage integration\n" +
                        "- Theme switching\n\n" +
                        "Developed by: [Your Name]\n" +
                        "Year: 2024"
        );
        aboutAlert.showAndWait();
    }

    @FXML
    protected void lightTheme(ActionEvent event) {
        Scene scene = menuBar.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
    }

    @FXML
    protected void darkTheme(ActionEvent event) {
        Scene scene = menuBar.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
    }

    public void closeApplication(ActionEvent actionEvent) {
    }

    public void logOut(ActionEvent actionEvent) {

    }
    public void showStatusMessage(String message, int duration) {
        statusLabel.setText(message); // Set the message
        statusLabel.setVisible(true); // Ensure the label is visible

        // Hide the message after the specified duration
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> statusLabel.setText(""));
        pause.play();
    }

    /**
     * Example method to demonstrate data addition success.
     */
    public void addData() {
        // Add your data processing logic here
        boolean success = true; // Example success flag

        if (success) {
            showStatusMessage("Data successfully added!", 5);
        } else {
            showStatusMessage("Failed to add data!", 5);
        }
    }
    public void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Window window = statusLabel.getScene().getWindow(); // Assuming statusLabel is defined
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                List<String[]> data = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    data.add(line.split(","));
                }

                // Process the data as needed
                showStatusMessage("Data successfully imported!", 5);
                System.out.println("Imported Data: " + data);

            } catch (IOException e) {
                showStatusMessage("Failed to import data.", 5);
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles exporting data to a CSV file.
     */
    public void handleExportCSV() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Window window = statusLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                // Sample data to export (replace with actual data)
                List<String[]> dataToExport = Arrays.asList(
                        new String[]{"ID", "Name", "Age"},
                        new String[]{"1", "Alice", "30"},
                        new String[]{"2", "Bob", "25"}
                );

                for (String[] row : dataToExport) {
                    writer.write(String.join(",", row));
                    writer.newLine();
                }

                showStatusMessage("Data successfully exported!", 5);
                System.out.println("Exported Data: " + dataToExport);

            } catch (IOException e) {
                showStatusMessage("Failed to export data.", 5);
                e.printStackTrace();
            }
        }
    }
}