package Controller.marketplaceManagement;

import model.category;
import model.product;
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
import model.user;
import services.marketPlace.CategoryService;
import services.marketPlace.ProductService;
import services.SessionManager;
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
    @FXML private BorderPane root;

    private product productToEdit = null;
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private String selectedImageFilename;

    @FXML
    public void initialize() {
        imageButton.setOnAction(e -> selectImage());
        saveButton.setOnAction(e -> saveProduct());
        loadCategories();
        setDefaultImage();
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
            if (defaultImage != null) {
                imagePreview.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.out.println("Default image could not be loaded: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<category> categories = categoryService.getAll();
            ObservableList<category> categoryList = FXCollections.observableArrayList(categories);
            categoryComboBox.setItems(categoryList);

            categoryComboBox.setConverter(new StringConverter<category>() {
                @Override
                public String toString(category category) {
                    return category != null ? category.getNameCategory() : "";
                }

                @Override
                public category fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error loading categories: " + e.getMessage());
        }
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        // Start in a common directory
        fileChooser.setInitialDirectory(new File("C:/Users/azizz/Downloads"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Define htdocs uploads directory path
                Path destDir = Paths.get("C:/xampp/htdocs/uploads");
                // Create directory if it doesn't exist
                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }

                // Generate unique filename to prevent overwriting
                String timestamp = String.valueOf(System.currentTimeMillis());
                String originalFilename = file.getName();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                selectedImageFilename = timestamp + extension;

                Path destPath = destDir.resolve(selectedImageFilename);
                Files.copy(file.toPath(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                imageLabel.setText(selectedImageFilename);

                // Preview the image
                imagePreview.setImage(new Image(file.toURI().toString()));

            } catch (IOException ex) {
                ex.printStackTrace();
                imageLabel.setText("Upload failed");
                showAlert(Alert.AlertType.ERROR, "Failed to process image: " + ex.getMessage());
            }
        }
    }


    private void saveProduct() {
        if (!validateInputs()) return;
        try {
            product p = new product();

            if (productToEdit != null) {
                p.setId(productToEdit.getId());
                p.setFarmerId(productToEdit.getFarmerId()); // Conserve l'ID existant
            } else {
                // Récupérer l'utilisateur connecté depuis le SessionManager
                user currentUser = SessionManager.getCurrentUser();

                // Vérifier si l'utilisateur est connecté
                if (currentUser == null) {
                    showAlert(Alert.AlertType.ERROR, "❌ Vous n'êtes pas connecté. Veuillez vous connecter avant d'ajouter un produit.");
                    return;
                }

                // Vérifier si l'utilisateur a le rôle "farmer"
                if (!"agriculteur".equalsIgnoreCase(currentUser.getRole())) {
                    showAlert(Alert.AlertType.ERROR, "❌ Seuls les agriculteurs peuvent ajouter des produits.");
                    return;
                }

                // Utiliser l'ID de l'utilisateur connecté
                p.setFarmerId(currentUser.getId());
            }

            p.setProdImg("http://localhost/uploads/" + selectedImageFilename);
            p.setNameProd(nameField.getText());
            p.setDescriptionProd(descriptionField.getText());
            p.setPriceProd(Double.parseDouble(priceField.getText()));
            p.setQuantity(Integer.parseInt(quantityField.getText()));

            category selectedCategory = categoryComboBox.getValue();
            if (selectedCategory != null) {
                p.setCategoryProdId(selectedCategory.getId());
            }

            if (productToEdit == null) {
                productService.add(p);
            } else {
                productService.update(p);
            }

            showAlert(Alert.AlertType.INFORMATION, "✅ Produit enregistré avec succès!");
            handleToggleView();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Erreur lors de l'enregistrement du produit: " + e.getMessage());
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

        for (category cat : categoryComboBox.getItems()) {
            if (cat.getId() == p.getCategoryProdId()) {
                categoryComboBox.setValue(cat);
                break;
            }
        }

        String imageUrl = p.getProdImg();
        if (imageUrl != null && imageUrl.contains("/")) {
            selectedImageFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        } else {
            selectedImageFilename = imageUrl;
        }

        imageLabel.setText(selectedImageFilename);

        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("http")) {
                    imagePreview.setImage(new Image(imageUrl, true));
                } else {
                    File imgFile = new File("C:/xampp/htdocs/uploads/" + selectedImageFilename);
                    if (imgFile.exists()) {
                        imagePreview.setImage(new Image(imgFile.toURI().toString()));
                    } else {
                        setDefaultImage();
                    }
                }
            } else {
                setDefaultImage();
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            setDefaultImage();
        }
    }

    @FXML
    private void handleToggleView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/marketplaceManagement/product_list.fxml"
            ));
            Region listView = loader.load();

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