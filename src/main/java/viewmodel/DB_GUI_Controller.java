package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Major2;
import model.Person;
import service.MyLogger;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.net.URL;
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
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);


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
        Major2 selectedMajor = Major2.valueOf(majorDropdown.getValue().toUpperCase()); // Map to Enum
        Person p = new Person(
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                selectedMajor,
                email.getText(),
                imageURL.getText()
        );
        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            return;
        }
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        majorDropdown.setValue(p.getMajor().getDisplayName()); // Use Display Name
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
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
        // Get the selected person from the TableView
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) {
            return; // No item selected, exit
        }

        int index = data.indexOf(p);

        // Get the selected major from the dropdown
        Major2 selectedMajor = Major2.valueOf(majorDropdown.getValue().toUpperCase());

        // Create a new Person object with updated values
        Person p2 = new Person(
                p.getId(), // Keep the original ID
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                selectedMajor, // Use the selected Major2 value
                email.getText(),
                imageURL.getText()
        );

        // Update the database and refresh the TableView
        cnUtil.editUser(p.getId(), p2); // Update database record
        data.set(index, p2); // Update TableView data
        tv.getSelectionModel().select(index); // Select the updated item in the table
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
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
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

}