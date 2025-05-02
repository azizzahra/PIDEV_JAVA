package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class CategoryController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Button btnToggleView;
    @FXML private Button saveButton;
    @FXML private Label formTitleLabel;

    private category currentCategory;
    private CategoryService categoryService = new CategoryService();
    private boolean isEditMode = false;
    private CategoryListController listController;

    @FXML
    public void initialize() {
        // Any initialization code here
    }

    public void setListController(CategoryListController listController) {
        this.listController = listController;
    }

    public void setCategoryToEdit(category c) {
        this.currentCategory = c;
        this.isEditMode = true;

        // Update form title to reflect edit mode
        if (formTitleLabel != null) {
            formTitleLabel.setText("🏷️ Edit Category");
        }

        nameField.setText(c.getNameCategory());
        descriptionField.setText(c.getDescriptionCategory());
    }

    @FXML
    private void handleToggleView() {
        try {
            // First check if we can use the listController reference
            if (listController != null) {
                listController.showCategoryList();
                return;
            }

            // If listController is null, try alternative navigation
            if (btnToggleView == null || btnToggleView.getScene() == null) {
                throw new NullPointerException("Button or scene is null");
            }

            // Find the contentArea from the scene root instead of direct lookup
            javafx.scene.Parent root = btnToggleView.getScene().getRoot();
            StackPane contentArea = null;

            // Navigate through scene graph to find the contentArea
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
                if (borderPane.getCenter() instanceof StackPane) {
                    contentArea = (StackPane) borderPane.getCenter();
                }
            } else {
                // Try generic lookup
                contentArea = (StackPane) root.lookup("#contentArea");
            }

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/devmasters/agrosphere/marketplaceManagement/category_list.fxml"
                ));
                Parent listView = loader.load();
                contentArea.getChildren().setAll(listView);
            } else {
                throw new IOException("Content area not found in scene graph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not return to category list: " + e.getMessage());
        }
    }

    @FXML
    private void saveCategory() {
        if (!validateCategoryInputs()) return;

        try {
            category c = (currentCategory == null) ? new category() : currentCategory;
            c.setNameCategory(nameField.getText().trim());
            c.setDescriptionCategory(descriptionField.getText().trim());

            if (currentCategory == null) {
                categoryService.add(c);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
            } else {
                categoryService.update(c);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
            }

            // Clear fields after saving
            if (!isEditMode) {
                nameField.clear();
                descriptionField.clear();
            }

            // Navigation after save - first try to use listController if available
            if (listController != null) {
                listController.loadCategories(); // Reload the list with updated data
                listController.showCategoryList(); // Navigate back to the list view
                return;
            }

            // If listController is null, use the alternative navigation approach
            navigateToListView();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to save category: " + e.getMessage());
        }
    }

    // New method to separate navigation logic
    private void navigateToListView() {
        try {
            if (saveButton == null || saveButton.getScene() == null) {
                throw new NullPointerException("Button or scene is null");
            }

            // Find the contentArea through reliable scene graph traversal
            javafx.scene.Parent root = saveButton.getScene().getRoot();
            StackPane contentArea = null;

            // Try direct traversal first
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
                if (borderPane.getCenter() instanceof StackPane) {
                    contentArea = (StackPane) borderPane.getCenter();
                }
            }

            // Fall back to lookup if needed
            if (contentArea == null) {
                contentArea = (StackPane) root.lookup("#contentArea");
            }

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/devmasters/agrosphere/marketplaceManagement/category_list.fxml"
                ));
                Parent listView = loader.load();

                // Get the controller and tell it to reload data
                CategoryListController listController = loader.getController();
                listController.loadCategories();

                contentArea.getChildren().setAll(listView);
            } else {
                throw new IOException("Content area not found in scene graph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to category list after save: " + e.getMessage());
        }
    }

    private boolean validateCategoryInputs() {
        StringBuilder errors = new StringBuilder();

        String name = nameField.getText().trim();
        String desc = descriptionField.getText().trim();

        if (name.isEmpty()) {
            errors.append("- Category name is required.\n");
        } else if (!name.matches("[A-Za-z ]+")) {
            errors.append("- Category name must contain only letters.\n");
        }

        if (desc.isEmpty()) {
            errors.append("- Description is required.\n");
        } else if (desc.length() < 5) {
            errors.append("- Description must be at least 5 characters long.\n");
        }

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Validation",
                    "Please correct the following errors:\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}