package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import com.devmasters.agrosphere.userManagement.entities.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import services.marketPlace.ProductService;
import services.marketPlace.CategoryService;
import services.user.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProductController {

    @FXML private Button imageButton;
    @FXML private Button saveButton;
    @FXML private Label imageLabel;
    @FXML private ImageView imagePreview;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<category> categoryComboBox;
    @FXML private ComboBox<user> farmerComboBox;
    @FXML private BorderPane root;

    private product productToEdit = null;
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final UserService userService = new UserService();
    private String selectedImageFilename;

    @FXML
    public void initialize() {
        // Setup buttons
        imageButton.setOnAction(e -> selectImage());
        saveButton.setOnAction(e -> saveProduct());

        // Load categories into ComboBox
        loadCategories();

        // Load farmers into ComboBox
        loadFarmers();

        // Set default image
        setDefaultImage();
    }

    private void setDefaultImage() {
        try {
            // Set a default placeholder image
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
            if (defaultImage != null) {
                imagePreview.setImage(defaultImage);
            }
        } catch (Exception e) {
            // If default image loading fails, just continue
            System.out.println("Default image could not be loaded: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<category> categories = categoryService.getAll();
            ObservableList<category> categoryList = FXCollections.observableArrayList(categories);
            categoryComboBox.setItems(categoryList);

            // Configure how category objects are displayed in ComboBox
            categoryComboBox.setConverter(new StringConverter<category>() {
                @Override
                public String toString(category category) {
                    return category != null ? category.getNameCategory() : "";
                }

                @Override
                public category fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error loading categories: " + e.getMessage());
        }
    }

    private void loadFarmers() {
        try {
            List<user> farmers = userService.getAllByRole("agriculteur");
            ObservableList<user> farmerList = FXCollections.observableArrayList(farmers);
            farmerComboBox.setItems(farmerList);

            farmerComboBox.setConverter(new StringConverter<user>() {
                @Override
                public String toString(user user) {
                    return user != null ? user.getPrenom() + " " + user.getNom() : "";
                }

                @Override
                public user fromString(String string) {
                    return null; // Not needed
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error loading farmers: " + e.getMessage());
        }
    }


    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.setInitialDirectory(new File("C:/Users/zeine/OneDrive/Pictures/pi"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Path destDir = Paths.get("uploads");
                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }

                selectedImageFilename = file.getName();
                imageLabel.setText(selectedImageFilename);

                // Preview
                Image img = new Image(file.toURI().toString());
                imagePreview.setImage(img);

            } catch (IOException ex) {
                ex.printStackTrace();
                imageLabel.setText("Upload failed");
                showAlert(Alert.AlertType.ERROR, "Failed to process image: " + ex.getMessage());
            }
        }
    }

    private void saveProduct() {
        if (!validateInputs()) return; // Stop if inputs are invalid
        try {
            product p = new product();

            if (productToEdit != null) {
                p.setId(productToEdit.getId());
            }

            p.setProdImg(selectedImageFilename);
            p.setNameProd(nameField.getText());
            p.setDescriptionProd(descriptionField.getText());
            p.setPriceProd(Double.parseDouble(priceField.getText()));
            p.setQuantity(Integer.parseInt(quantityField.getText()));

            // Get selected category and farmer from ComboBoxes
            category selectedCategory = categoryComboBox.getValue();
            user selectedFarmer = farmerComboBox.getValue();

            if (selectedCategory != null) {
                p.setCategoryProdId(selectedCategory.getId());
            }

            if (selectedFarmer != null) {
                p.setFarmerId(selectedFarmer.getId());
            }

            if (productToEdit == null) {
                productService.add(p);
            } else {
                productService.update(p);
            }

            showAlert(Alert.AlertType.INFORMATION, "✅ Product saved successfully!");

            // Return to product list
            handleToggleView();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error while saving product: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    public void setProductToEdit(product p) {
        this.productToEdit = p;

        nameField.setText(p.getNameProd());
        descriptionField.setText(p.getDescriptionProd());
        priceField.setText(String.valueOf(p.getPriceProd()));
        quantityField.setText(String.valueOf(p.getQuantity()));

        // Set the category in ComboBox
        for (category cat : categoryComboBox.getItems()) {
            if (cat.getId() == p.getCategoryProdId()) {
                categoryComboBox.setValue(cat);
                break;
            }
        }

        // Set the farmer in ComboBox
        for (user farmer : farmerComboBox.getItems()) {
            if (farmer.getId() == p.getFarmerId()) {
                farmerComboBox.setValue(farmer);
                break;
            }
        }

        imageLabel.setText(p.getProdImg());
        selectedImageFilename = p.getProdImg();

        File imgFile = new File("uploads/" + selectedImageFilename);
        if (imgFile.exists()) {
            imagePreview.setImage(new Image(imgFile.toURI().toString()));
        }
    }

    @FXML
    private void handleToggleView() {
        try {
            // Get reference to the DashboardProductController
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagement/product_list.fxml"
            ));
            Region listView = loader.load();

            // Find the StackPane contentArea in the root BorderPane
            StackPane contentArea = (StackPane) nameField.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(listView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load product list: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Product name is required.\n");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("- Description is required.\n");
        }
        if (priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required.\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) errors.append("- Price must be greater than 0.\n");
            } catch (NumberFormatException e) {
                errors.append("- Price must be a number.\n");
            }
        }

        if (quantityField.getText().trim().isEmpty()) {
            errors.append("- Quantity is required.\n");
        } else {
            try {
                int qty = Integer.parseInt(quantityField.getText());
                if (qty < 0) errors.append("- Quantity cannot be negative.\n");
            } catch (NumberFormatException e) {
                errors.append("- Quantity must be an integer.\n");
            }
        }

        if (categoryComboBox.getValue() == null) {
            errors.append("- Please select a category.\n");
        }

        if (farmerComboBox.getValue() == null) {
            errors.append("- Please select a farmer.\n");
        }

        if (selectedImageFilename == null || selectedImageFilename.trim().isEmpty()) {
            errors.append("- An image must be selected.\n");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input Validation");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
}