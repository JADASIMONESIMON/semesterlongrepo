package viewmodel;

import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Major2;
import model.Person;
import service.MyLogger;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML
    TextField first_name, last_name, department, email, imageURL;

    @FXML
    private Button editButton;

    @FXML
    private ComboBox<String> majorDropdown;

    @FXML
    private MenuItem importCSVMenuItem;

    @FXML
    private MenuItem exportCSVMenuItem;

    @FXML
    private ImageView simonLogo;




    @FXML
    private Button addbutton;

    @FXML
    private Button deleteButton;

    @FXML
    private MenuItem editItem;

    @FXML
    private MenuItem deleteItem;

    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private Label statusBar;

    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private ObservableList<String> majorOptions;
    private MouseEvent mouseEvent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            simonLogo.setImage(new Image(getClass().getResourceAsStream("/images/eduenrolllogo.jpg")));
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);


            importCSVMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
            exportCSVMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
            editItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+E"));
            deleteItem.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));

            // Add event handlers if necessary
            importCSVMenuItem.setOnAction(event -> importCSV(event));
            exportCSVMenuItem.setOnAction(event -> exportCSV(event));
            editItem.setOnAction(event -> editRecord());
            deleteItem.setOnAction(event -> deleteRecord());

            MyLogger.makeLog("Menu shortcuts initialized.");



            // Initial check to apply correct style
            updateTableViewStyle(tv);


            // Populate the dropdown
            majorDropdown.setItems(FXCollections.observableArrayList(
                    Major2.CS.name(),
                    Major2.CPIS.name(),
                    Major2.ENGLISH.name()
            ));


            BooleanBinding noSelection = tv.getSelectionModel().selectedItemProperty().isNull();
            editButton.disableProperty().bind(noSelection); // Edit button disabled unless a record is selected
            deleteButton.disableProperty().bind(noSelection); // Delete button disabled unless a record is selected

            // Bind menu items to selection
            //editItem.disableProperty().bind(noSelection);
            //deleteItem.disableProperty().bind(noSelection);

            // Validate form fields for Add button
            BooleanBinding formInvalid = first_name.textProperty().isEmpty()
                    .or(last_name.textProperty().isEmpty())
                    .or(department.textProperty().isEmpty())
                    .or(majorDropdown.valueProperty().isNull())
                    .or(email.textProperty().isEmpty().or(email.textProperty().isNotEqualTo("@").not()));
            addbutton.disableProperty().bind(formInvalid); // Add button enabled only when form is valid

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }
    /**
     * Validates email format using regex.
     */
    private BooleanBinding emailValid(StringProperty emailProperty) {
        String emailRegex = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return Bindings.createBooleanBinding(() -> emailProperty.get().matches(emailRegex), emailProperty);
    }

    /**
     * Validates text fields for names and departments using regex.
     * - No special characters except spaces.
     * - Must start with a letter.
     */
    private BooleanBinding textFieldValid(StringProperty textProperty) {
        String textRegex = "^[a-zA-Z][a-zA-Z\\s]*$";
        return Bindings.createBooleanBinding(() -> textProperty.get().matches(textRegex), textProperty);
    }





    @FXML
    protected void addNewRecord() {
        try {
            Major2 selectedMajor = Major2.valueOf(majorDropdown.getValue().toUpperCase());
            String uploadedImageURL = imageURL.getText(); // Use the uploaded URL

            Person p = new Person(
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    selectedMajor,
                    email.getText(),
                    uploadedImageURL // Link the uploaded image URL
            );
            cnUtil.insertUser(p); // Save to the database
            p.setId(cnUtil.retrieveId(p)); // Retrieve the generated ID
            data.add(p);
            clearForm();

            updateStatus("Record added successfully!", true);
        } catch (Exception e) {
            updateStatus("Error adding record: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }





    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            // Update form fields
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            majorDropdown.setValue(p.getMajor() != null ? p.getMajor().name() : null);
            email.setText(p.getEmail());

            // Validate and update ImageView
            String imageUrl = p.getImageURL();
            if (imageUrl != null && !imageUrl.isBlank()) {
                try {
                    img_view.setImage(new Image(imageUrl, true)); // Load asynchronously
                    updateStatus("Image updated for selected record.", true);
                } catch (IllegalArgumentException e) {
                    img_view.setImage(null); // Clear the image view if the URL is invalid
                    updateStatus("Invalid image URL for selected record.", false);
                }
            } else {
                img_view.setImage(null); // Clear the image view if no URL is set
                updateStatus("No image available for selected record.", true);
            }
        }
    }







    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorDropdown.getSelectionModel().clearSelection(); // Clear dropdown
        email.setText("");
        imageURL.setText("");
    }


    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            if (p == null) {
                updateStatus("No record selected for editing.", false);
                return;
            }
            int index = data.indexOf(p);
            Major2 selectedMajor = Major2.valueOf(majorDropdown.getValue().toUpperCase());
            String uploadedImageURL = imageURL.getText(); // Use the uploaded URL

            Person updatedPerson = new Person(
                    p.getId(),
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    selectedMajor,
                    email.getText(),
                    uploadedImageURL // Link the updated image URL
            );
            cnUtil.editUser(p.getId(), updatedPerson); // Update the database record
            data.set(index, updatedPerson);

            updateStatus("Record updated successfully!", true);
        } catch (Exception e) {
            updateStatus("Error updating record: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }








    @FXML
    private void importCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(","); // Split by comma
                    if (values.length != 6) continue; // Assuming 6 fields per record

                    // Create and add the Person
                    Person person = new Person(
                            values[0],  // First Name
                            values[1],  // Last Name
                            values[2],  // Department
                            Major2.valueOf(values[3].toUpperCase()), // Major
                            values[4],  // Email
                            values[5]   // Image URL
                    );
                    data.add(person);
                    cnUtil.insertUser(person); // Save to database
                }
                updateStatus("CSV file imported successfully!", true);
            } catch (Exception e) {
                updateStatus("Error importing CSV: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }

    // Export CSV
    @FXML
    private void exportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (selectedFile != null) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile))) {
                for (Person person : data) {
                    String record = String.join(",",
                            Arrays.asList(
                                    person.getFirstName(),
                                    person.getLastName(),
                                    person.getDepartment(),
                                    person.getMajor().name(),
                                    person.getEmail(),
                                    person.getImageURL()
                            ));
                    bw.write(record);
                    bw.newLine();
                }
                updateStatus("CSV file exported successfully!", true);
            } catch (Exception e) {
                updateStatus("Error exporting CSV: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }




    @FXML
    protected void deleteRecord() {

        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }


    @FXML
    protected void showImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            try {
                StorageUploader uploader = new StorageUploader();
                String blobName = "profiles/" + file.getName();
                uploader.uploadFile(file.getAbsolutePath(), blobName);

                String blobUrl = uploader.getContainerClient().getBlobClient(blobName).getBlobUrl();
                if (!blobUrl.isBlank()) {
                    img_view.setImage(new Image(blobUrl, true)); // Load asynchronously
                    imageURL.setText(blobUrl);
                    updateStatus("Image uploaded successfully!", true);
                } else {
                    updateStatus("Failed to retrieve Blob URL after upload.", false);
                }
            } catch (Exception e) {
                updateStatus("Error uploading image: " + e.getMessage(), false);
                e.printStackTrace();
            }
        } else {
            updateStatus("No file selected.", false);
        }
    }


    /**
     * Updates the TableView's style class based on whether it is empty.
     */
    private void updateTableViewStyle(TableView<Person> tv) {
        if (data.isEmpty()) {
            this.tv.getStyleClass().add("empty");
        } else {
            this.tv.getStyleClass().remove("empty");
        }
    }







    @FXML
    protected void addRecord() {
        showSomeone();
    }

    private void updateStatus(String message, boolean autoClear) {
        statusBar.setText("Status: " + message);

        // Auto-clear the status after 5 seconds
        if (autoClear) {
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(event -> statusBar.setText("Status: Ready"));
            pause.play();
        }
    }





    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            updateTableViewStyle(tv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }
    @FXML
    private VBox progressBarContainer;

    /**
     * Uploads an image and measures the time it takes, updating a progress bar in real-time.
     */
    @FXML
    protected void uploadImageWithProgress() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            ProgressBar progressBar = new ProgressBar(0);
            Label progressLabel = new Label("Uploading...");
            progressBarContainer.getChildren().addAll(progressLabel, progressBar);

            Task<Void> uploadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    long startTime = System.currentTimeMillis();

                    // Simulate uploading and update progress
                    StorageUploader uploader = new StorageUploader();
                    String blobName = "profiles/" + file.getName();
                    uploader.uploadFile(file.getAbsolutePath(), blobName);

                    String blobUrl = uploader.getContainerClient().getBlobClient(blobName).getBlobUrl();
                    if (!blobUrl.isBlank()) {
                        Platform.runLater(() -> {
                            img_view.setImage(new Image(blobUrl, true));
                            imageURL.setText(blobUrl);
                        });
                    }

                    long endTime = System.currentTimeMillis();
                    long uploadDuration = endTime - startTime;

                    Platform.runLater(() -> {
                        progressLabel.setText("Upload completed in " + uploadDuration + " ms");
                        progressBar.setProgress(1.0); // Complete progress
                    });

                    return null;
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        progressLabel.setText("Upload failed: " + getException().getMessage());
                    });
                }
            };

            // Bind progress bar to the task progress
            progressBar.progressProperty().bind(uploadTask.progressProperty());

            // Run the task in a background thread
            Thread uploadThread = new Thread(uploadTask);
            uploadThread.setDaemon(true);
            uploadThread.start();
        } else {
            updateStatus("No file selected.", false);
        }
    }


}