package Controller.marketplaceManagement;

import model.category;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

            // First try to use the listController for navigation if available
            if (listController != null) {
                try {
                    // Load the form via the list controller
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/marketplaceManagement/category_form.fxml"
                    ));
                    Parent formView = loader.load();

                    CategoryController controller = loader.getController();
                    controller.setCategoryToEdit(currentCategory);
                    controller.setListController(listController);

                    // Use the list controller to handle the navigation
                    listController.showCategoryForm(formView);
                    return;
                } catch (Exception e) {
                    // If this approach fails, we'll fall back to the scene graph approach
                    System.out.println("Could not navigate via list controller: " + e.getMessage());
                }
            }

            // Fallback approach: search for contentArea through the entire scene graph
            Scene scene = editButton.getScene();
            Parent root = scene.getRoot();
            StackPane contentArea = findContentArea(root);

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/marketplaceManagement/category_form.fxml"
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

    // Helper method to recursively search for the contentArea in the scene graph
    private StackPane findContentArea(Parent node) {
        // First check if this node is the contentArea
        if (node instanceof StackPane && "contentArea".equals(node.getId())) {
            return (StackPane) node;
        }

        // Check direct approach for BorderPane with StackPane center
        if (node instanceof BorderPane) {
            BorderPane borderPane = (BorderPane) node;
            if (borderPane.getCenter() instanceof StackPane) {
                StackPane center = (StackPane) borderPane.getCenter();
                if ("contentArea".equals(center.getId())) {
                    return center;
                }
            }
        }

        // Try lookup approach
        StackPane lookupResult = (StackPane) node.lookup("#contentArea");
        if (lookupResult != null) {
            return lookupResult;
        }

        // If direct approaches fail, recursively search through all children
        for (javafx.scene.Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                StackPane result = findContentArea((Parent) child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
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