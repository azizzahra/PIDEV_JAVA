package controller.Farm;

import Main.mainPrincipal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Farm;
import model.plante;
import services.FarmService;
import services.PlanteService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import java.util.prefs.Preferences;

public class AddPlanteController {

    @FXML
    private Label farmNameBreadcrumb;

    @FXML
    private TextField nameField;
    @FXML
    private Label nameError;

    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private Label typeError;

    @FXML
    private DatePicker plantationDatePicker;
    @FXML
    private Label plantationDateError;

    @FXML
    private DatePicker harvestDatePicker;
    @FXML
    private Label harvestDateError;

    @FXML
    private TextField quantityField;
    @FXML
    private Label quantityError;

    @FXML
    private Button chooseImageButton;

    @FXML
    private Label imagePathLabel;
    @FXML
    private Label imageError;
    @FXML
    private ImageView imagePreview;

    private int farmId;
    private File selectedImageFile;
    private String imageName;
    private FarmService farmService = new FarmService();
    private PlanteService planteService = new PlanteService();

    private Preferences prefs = Preferences.userNodeForPackage(AddFarmController.class);

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("Vegetables", "Fruits", "Flowers");

        chooseImageButton.setOnAction(e -> chooseImage());

        setupValidationListeners();
        hideAllErrorMessages();
    }

    public void setFarmId(int id) {
        this.farmId = id;
        try {
            Farm farm = farmService.getone(farmId);
            if (farm != null) {
                farmNameBreadcrumb.setText(farm.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load farm information.", Alert.AlertType.ERROR);
        }
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Plant Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        // Get last directory from preferences
        String lastDirectoryPath = prefs.get("lastImageDirectory", null);
        if (lastDirectoryPath != null) {
            File lastDir = new File(lastDirectoryPath);
            if (lastDir.exists()) {
                fileChooser.setInitialDirectory(lastDir);
            }
        }

        // Show file chooser dialog
        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            prefs.put("lastImageDirectory", selectedImageFile.getParent());
            imagePathLabel.setText(selectedImageFile.getName());

            imageName = System.currentTimeMillis() + "_" + selectedImageFile.getName();

            Image image = new Image(selectedImageFile.toURI().toString());
            imagePreview.setImage(image);

            imageError.setVisible(false);
        }
    }

    @FXML
    private void savePlant() {
        hideAllErrorMessages();

        if (!validateInputs()) {
            return;
        }

        try {
            if (planteService.planteExiste(nameField.getText(), farmId)) {
                showAlert("Error", "A plant with this name already exists in this farm.", Alert.AlertType.ERROR);
                return;
            }

            plante newPlant = new plante();
            newPlant.setFarmId(farmId);
            newPlant.setName(nameField.getText());
            newPlant.setType(typeComboBox.getValue());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (plantationDatePicker.getValue() != null) {
                newPlant.setPlantationDate(plantationDatePicker.getValue().format(formatter));
            }
            if (harvestDatePicker.getValue() != null) {
                newPlant.setHarvestDate(harvestDatePicker.getValue().format(formatter));
            }

            try {
                newPlant.setQuantity(Integer.parseInt(quantityField.getText()));
            } catch (NumberFormatException e) {
                showAlert("Error", "Quantity must be a valid number.", Alert.AlertType.ERROR);
                return;
            }

            if (selectedImageFile != null) {
                // Copy image to resources directory
                String resourcesDir = "C:/xampp/htdocs/uploads/plant_image/";
                Path targetPath = Paths.get(resourcesDir + imageName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                newPlant.setImage(imageName);
            } else {
                imageError.setText("An image is required");
                imageError.setVisible(true);
                chooseImageButton.requestFocus();
                return;
            }

            planteService.add(newPlant);
            showAlert("Success", "Plant added successfully!", Alert.AlertType.INFORMATION);
            returnToFarmDetails();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save plant: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }





    private void setupValidationListeners() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nameError.setText("Plant name is required");
                nameError.setVisible(true);
            } else if (newValue.trim().length() < 3) {
                nameError.setText("Plant name must have at least 3 characters");
                nameError.setVisible(true);
            } else {
                nameError.setVisible(false);
            }
        });

        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                typeError.setText("Plant type is required");
                typeError.setVisible(true);
            } else {
                typeError.setVisible(false);
            }
        });

        plantationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                plantationDateError.setText("Plantation date is required");
                plantationDateError.setVisible(true);
            } else {
                plantationDateError.setVisible(false);

                if (harvestDatePicker.getValue() != null &&
                        harvestDatePicker.getValue().isBefore(newValue)) {
                    harvestDateError.setText("Harvest date cannot be before plantation date");
                    harvestDateError.setVisible(true);
                } else {
                    harvestDateError.setVisible(false);
                }
            }
        });

        harvestDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                harvestDateError.setText("Harvest date is required");
                harvestDateError.setVisible(true);
            } else if (plantationDatePicker.getValue() != null &&
                    newValue.isBefore(plantationDatePicker.getValue())) {
                harvestDateError.setText("Harvest date cannot be before plantation date");
                harvestDateError.setVisible(true);
            } else {
                harvestDateError.setVisible(false);
            }
        });

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                quantityError.setText("Quantity is required");
                quantityError.setVisible(true);
            } else {
                try {
                    int quantity = Integer.parseInt(newValue.trim());
                    if (quantity <= 0) {
                        quantityError.setText("Quantity must be a positive number");
                        quantityError.setVisible(true);
                    } else {
                        quantityError.setVisible(false);
                    }
                } catch (NumberFormatException e) {
                    quantityError.setText("Please enter a valid number");
                    quantityError.setVisible(true);
                }
            }
        });
    }

    private void hideAllErrorMessages() {
        nameError.setVisible(false);
        typeError.setVisible(false);
        plantationDateError.setVisible(false);
        harvestDateError.setVisible(false);
        quantityError.setVisible(false);
        imageError.setVisible(false);
    }


    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameError.setText("Plant name is required");
            nameError.setVisible(true);
            nameField.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            nameError.setText("Plant name must have at least 3 characters");
            nameError.setVisible(true);
            nameField.requestFocus();
            isValid = false;
        }

        if (typeComboBox.getValue() == null) {
            typeError.setText("Plant type is required");
            typeError.setVisible(true);
            if (isValid) {
                typeComboBox.requestFocus();
                isValid = false;
            }
        }

        if (plantationDatePicker.getValue() == null) {
            plantationDateError.setText("Plantation date is required");
            plantationDateError.setVisible(true);
            if (isValid) {
                plantationDatePicker.requestFocus();
                isValid = false;
            }
        }

        if (harvestDatePicker.getValue() == null) {
            harvestDateError.setText("Harvest date is required");
            harvestDateError.setVisible(true);
            if (isValid) {
                harvestDatePicker.requestFocus();
                isValid = false;
            }
        } else if (plantationDatePicker.getValue() != null &&
                harvestDatePicker.getValue().isBefore(plantationDatePicker.getValue())) {
            harvestDateError.setText("Harvest date cannot be before plantation date");
            harvestDateError.setVisible(true);
            if (isValid) {
                harvestDatePicker.requestFocus();
                isValid = false;
            }
        }

        String quantityStr = quantityField.getText().trim();
        if (quantityStr.isEmpty()) {
            quantityError.setText("Quantity is required");
            quantityError.setVisible(true);
            if (isValid) {
                quantityField.requestFocus();
                isValid = false;
            }
        } else {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    quantityError.setText("Quantity must be a positive number");
                    quantityError.setVisible(true);
                    if (isValid) {
                        quantityField.requestFocus();
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                quantityError.setText("Please enter a valid number");
                quantityError.setVisible(true);
                if (isValid) {
                    quantityField.requestFocus();
                    isValid = false;
                }
            }
        }

        if (selectedImageFile == null) {
            imageError.setText("An image is required");
            imageError.setVisible(true);
            if (isValid) {
                chooseImageButton.requestFocus();
                isValid = false;
            }
        } else {
            String filename = selectedImageFile.getName().toLowerCase();
            if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                    filename.endsWith(".png") || filename.endsWith(".gif"))) {
                imageError.setText("Unsupported image format");
                imageError.setVisible(true);
                if (isValid) {
                    chooseImageButton.requestFocus();
                    isValid = false;
                }
            }
        }

        return isValid;
    }


    private void returnToFarmDetails() {
        try {
            // Use the main stage
            Stage mainStage = mainPrincipal.getPrimaryStage();

            // Load the farm details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Farm/FarmDetails.fxml"));
            Parent root = loader.load();

            // Configure the controller with the farm ID
            FarmDetailsController controller = loader.getController();
            controller.setFarmId(farmId);

            // Replace the content of the main stage
            mainStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to farm details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancel() {
        returnToFarmDetails();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}