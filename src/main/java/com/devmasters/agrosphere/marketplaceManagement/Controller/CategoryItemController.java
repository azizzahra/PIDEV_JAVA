package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;

public class CategoryItemController {

    @FXML private Label nameLabel;
    @FXML private Label descLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private category currentCategory;
    private CategoryListController listController;
    private CategoryService categoryService = new CategoryService();

    public void setData(category c, CategoryListController controller) {
        this.currentCategory = c;
        this.listController = controller;

        nameLabel.setText(c.getNameCategory());

        // Truncate description if too long for display
        String description = c.getDescriptionCategory();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        descLabel.setText(description);
    }

    @FXML
    private void onEditClicked() {
        try {
            if (editButton == null || editButton.getScene() == null) {
                throw new NullPointerException("Button or scene is null");
            }

            // Get the root node and find contentArea more reliably
            Parent root = editButton.getScene().getRoot();
            StackPane contentArea = null;

            // Try to find contentArea through scene graph traversal
            if (root instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) root;
                if (borderPane.getCenter() instanceof StackPane) {
                    contentArea = (StackPane) borderPane.getCenter();
                }
            } else {
                // Fall back to lookup if direct traversal fails
                contentArea = (StackPane) root.lookup("#contentArea");
            }

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/devmasters/agrosphere/marketplaceManagement/category_form.fxml"
                ));
                Parent formView = loader.load();

                CategoryController controller = loader.getController();
                controller.setCategoryToEdit(currentCategory);
                controller.setListController(listController);

                contentArea.getChildren().setAll(formView);
            } else {
                throw new Exception("Content area not found in scene graph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open edit form: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteClicked() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Category");
        confirmation.setContentText("Are you sure you want to delete the category '" +
                currentCategory.getNameCategory() + "'?");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmation.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirmation.showAndWait().ifPresent(type -> {
            if (type == buttonTypeYes) {
                try {
                    categoryService.delete(currentCategory.getId());
                    listController.loadCategories();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Delete Error",
                            "Failed to delete category: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}