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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UpdatePlanteController {

    @FXML
    private Label farmNameBreadcrumb;

    @FXML
    private Label planteNameBreadcrumb;

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

    private int planteId;
    private int farmId;
    private File selectedImageFile;
    private String imageName;
    private String currentImageName;
    private FarmService farmService = new FarmService();
    private PlanteService planteService = new PlanteService();
    private plante currentPlante;

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("Vegetables", "Fruits", "Flowers");

        // Setup image button
        chooseImageButton.setOnAction(e -> chooseImage());

        setupValidationListeners();
        hideAllErrorMessages();
    }


    public void setPlanteId(int id) {
        this.planteId = id;
        loadPlanteDetails();
    }

    public void setFarmId(int id) {
        this.farmId = id;
        try {
            // Load farm name for breadcrumb
            Farm farm = farmService.getone(farmId);
            if (farm != null) {
                farmNameBreadcrumb.setText(farm.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load farm information.", Alert.AlertType.ERROR);
        }
    }

    private void loadPlanteDetails() {
        try {
            // Load plant details
            currentPlante = planteService.getone(planteId);

            if (currentPlante != null) {
                // Set breadcrumb
                planteNameBreadcrumb.setText(currentPlante.getName());

                // Fill form with current values
                nameField.setText(currentPlante.getName());
                typeComboBox.setValue(currentPlante.getType());

                // Set dates
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (currentPlante.getPlantationDate() != null && !currentPlante.getPlantationDate().isEmpty()) {
                    plantationDatePicker.setValue(LocalDate.parse(currentPlante.getPlantationDate(), formatter));
                }

                if (currentPlante.getHarvestDate() != null && !currentPlante.getHarvestDate().isEmpty()) {
                    harvestDatePicker.setValue(LocalDate.parse(currentPlante.getHarvestDate(), formatter));
                }

                // Set quantity
                quantityField.setText(String.valueOf(currentPlante.getQuantity()));

                // Load image
                currentImageName = currentPlante.getImage();
                if (currentImageName != null && !currentImageName.isEmpty()) {
                    imagePathLabel.setText(currentImageName);
                    loadImagePreview(currentImageName);
                }
            } else {
                showAlert("Error", "Could not find plant with ID: " + planteId, Alert.AlertType.ERROR);
                returnToFarmDetails();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load plant details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadImagePreview(String imageName) {
        try {
            String imagePath = "file:C:/xampp/htdocs/uploads/plant_image/" + imageName;
            Image image = new Image(imagePath);
            imagePreview.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Plant Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Show file chooser dialog
        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imagePathLabel.setText(selectedImageFile.getName());

            // Generate unique image name to prevent overwriting
            imageName = System.currentTimeMillis() + "_" + selectedImageFile.getName();

            // Display image preview
            Image image = new Image(selectedImageFile.toURI().toString());
            imagePreview.setImage(image);

            // Validation pour image OK
            imageError.setVisible(false);
        }
    }

    @FXML
    private void updatePlant() {
        hideAllErrorMessages();

        if (!validateInputs()) {
            return;
        }

        try {
            // Check if plant name already exists (other than this plant)
            if (!nameField.getText().equals(currentPlante.getName()) &&
                    planteService.planteExiste(nameField.getText(), farmId)) {
                showAlert("Error", "A plant with this name already exists in this farm.", Alert.AlertType.ERROR);
                return;
            }

            // Update plant object
            currentPlante.setName(nameField.getText());
            currentPlante.setType(typeComboBox.getValue());

            // Format dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (plantationDatePicker.getValue() != null) {
                currentPlante.setPlantationDate(plantationDatePicker.getValue().format(formatter));
            }
            if (harvestDatePicker.getValue() != null) {
                currentPlante.setHarvestDate(harvestDatePicker.getValue().format(formatter));
            }

            // Set quantity
            try {
                currentPlante.setQuantity(Integer.parseInt(quantityField.getText()));
            } catch (NumberFormatException e) {
                showAlert("Error", "Quantity must be a valid number.", Alert.AlertType.ERROR);
                return;
            }

            // Handle image
            if (selectedImageFile != null) {
                // Copy image to resources directory
                String resourcesDir = "C:/xampp/htdocs/";
                Path targetPath = Paths.get(resourcesDir + imageName);
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                currentPlante.setImage(imageName);
            }

            // Save updated plant
            planteService.update(currentPlante);

            // Show success message
            showAlert("Success", "Plant updated successfully!", Alert.AlertType.INFORMATION);

            // Return to farm details
            returnToFarmDetails();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update plant: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void hideAllErrorMessages() {
        nameError.setVisible(false);
        typeError.setVisible(false);
        plantationDateError.setVisible(false);
        harvestDateError.setVisible(false);
        quantityError.setVisible(false);
        imageError.setVisible(false);
    }

    private void setupValidationListeners() {
        // Validation en temps réel pour le nom
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

        // Validation en temps réel pour le type
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                typeError.setText("Plant type is required");
                typeError.setVisible(true);
            } else {
                typeError.setVisible(false);
            }
        });

        // Validation en temps réel pour la date de plantation
        plantationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                plantationDateError.setText("Plantation date is required");
                plantationDateError.setVisible(true);
            } else {
                plantationDateError.setVisible(false);

                // Vérifier la cohérence avec la date de récolte si elle est définie
                if (harvestDatePicker.getValue() != null &&
                        harvestDatePicker.getValue().isBefore(newValue)) {
                    harvestDateError.setText("Harvest date cannot be before plantation date");
                    harvestDateError.setVisible(true);
                } else {
                    harvestDateError.setVisible(false);
                }
            }
        });

        // Validation en temps réel pour la date de récolte
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

        // Validation en temps réel pour la quantité
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

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du nom (obligatoire, min 3 caractères)
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

        // Validation du type (obligatoire)
        if (typeComboBox.getValue() == null) {
            typeError.setText("Plant type is required");
            typeError.setVisible(true);
            if (isValid) {
                typeComboBox.requestFocus();
                isValid = false;
            }
        }

        // Validation de la date de plantation (obligatoire)
        if (plantationDatePicker.getValue() == null) {
            plantationDateError.setText("Plantation date is required");
            plantationDateError.setVisible(true);
            if (isValid) {
                plantationDatePicker.requestFocus();
                isValid = false;
            }
        }

        // Validation de la date de récolte (obligatoire et doit être après la date de plantation)
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

        // Validation de la quantité (obligatoire, doit être un nombre positif)
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

        // La validation d'image n'est pas obligatoire en mode modification car l'image peut déjà exister
        if (selectedImageFile != null) {
            // Vérifier si c'est un format d'image valide
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
        // Return to farm details without saving
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