package controller;

import entities.Place;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.PlaceService;

import java.io.File;
import java.sql.SQLException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AddPlaceController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField capacityField;
    @FXML private TextField imageField;  // This will show the selected image path
    @FXML private Button selectImageButton;  // Button to trigger image selection

    private PlaceService placeService;
    private Runnable refreshCallback;

    @FXML
    public void initialize() {
        // Set up input validation and formatting
        configureNumericField(priceField, true);  // Allow decimals for price
        configureNumericField(capacityField, false); // Integers only for capacity

        // Basic validation for image URL (optional)
        imageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !isValidImagePath(newVal)) {
                imageField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
            } else {
                imageField.setStyle("");
            }
        });
    }

    public void setPlaceService(PlaceService placeService) {
        this.placeService = placeService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            Place newPlace = new Place(
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(capacityField.getText()),
                    imageField.getText().trim()  // Use selected image path
            );
            placeService.ajouter(newPlace);  // Assuming this method adds the place to the database
            refreshCallback.run();  // Refresh the list or UI if necessary
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for price and capacity");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add place: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleSelectImage() {
        // Set up file chooser to allow only image files
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));

        Stage stage = (Stage) selectImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Check if the file exists at the selected path
            if (selectedFile.exists()) {
                // Set the selected image file path to the imageField
                imageField.setText(selectedFile.getAbsolutePath());
            } else {
                System.out.println("Error: Selected file does not exist.");
            }
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Name is required\n");
            nameField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            nameField.setStyle("");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required\n");
            priceField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            priceField.setStyle("");
        }

        if (capacityField.getText().trim().isEmpty()) {
            errors.append("- Capacity is required\n");
            capacityField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            capacityField.setStyle("");
        }

        if (!imageField.getText().isEmpty() && !isValidImagePath(imageField.getText())) {
            errors.append("- Invalid image file path\n");
            imageField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            imageField.setStyle("");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void configureNumericField(TextField field, boolean allowDecimal) {
        Pattern pattern = allowDecimal ? Pattern.compile("\\d*\\.?\\d*") : Pattern.compile("\\d*");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private boolean isValidImagePath(String path) {
        // Basic validation - checks if the path ends with an image extension
        return path.matches("^(.*\\.(jpg|jpeg|png|gif|bmp))$");
    }

    private void closeWindow() {
        nameField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
