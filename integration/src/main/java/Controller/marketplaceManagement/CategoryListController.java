package Controller.marketplaceManagement;

import model.category;
import javafx.scene.layout.BorderPane;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class CategoryListController {

    @FXML private VBox categoryContainer;
    @FXML private TextField categorySearchField;
    @FXML private ComboBox<String> categorySortCombo;
    @FXML private Button btnAddCategory;

    // Reference to the contentArea
    @FXML private StackPane contentArea;

    private CategoryService categoryService = new CategoryService();

    // Reference to the dashboard controller for navigation
    private DashboardProductController dashboardController;

    @FXML
    public void initialize() {
        // Setup event listeners
        if (categorySearchField != null) {
            categorySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterCategories(newValue);
            });
        }

        if (categorySortCombo != null) {
            categorySortCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    sortCategories(newValue);
                }
            });
        }

        // Load categories initially
        loadCategories();
    }

    /**
     * Loads all categories from the service and displays them
     */
    public void loadCategories() {
        try {
            categoryContainer.getChildren().clear();
            List<category> categories = categoryService.getAll();

            if (categories.isEmpty()) {
                Label emptyLabel = new Label("No categories found. Add one to get started!");
                emptyLabel.getStyleClass().add("empty-message");
                categoryContainer.getChildren().add(emptyLabel);
            } else {
                for (category c : categories) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                "/marketplaceManagement/category_item.fxml"));
                        Parent categoryItem = loader.load();

                        CategoryItemController itemController = loader.getController();
                        itemController.setData(c, this);

                        categoryContainer.getChildren().add(categoryItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load categories: " + e.getMessage());
        }
    }

    /**
     * Filters the category list based on search text
     */
    private void filterCategories(String searchText) {
        // Implementation for filtering categories
        // This would reload the list with filtered results
        try {
            categoryContainer.getChildren().clear();
            List<category> categories = categoryService.searchByName(searchText);

            if (categories.isEmpty()) {
                Label emptyLabel = new Label("No categories match your search.");
                emptyLabel.getStyleClass().add("empty-message");
                categoryContainer.getChildren().add(emptyLabel);
            } else {
                for (category c : categories) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                "/marketplaceManagement/category_item.fxml"));
                        Parent categoryItem = loader.load();

                        CategoryItemController itemController = loader.getController();
                        itemController.setData(c, this);

                        categoryContainer.getChildren().add(categoryItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sorts the category list based on the selected sort option
     */
    private void sortCategories(String sortOption) {
        // Implementation for sorting categories
        // This would reload the list with sorted results
        try {
            categoryContainer.getChildren().clear();
            List<category> categories;

            if (sortOption.equals("Name (A-Z)")) {
                categories = categoryService.getAllSortedAsc();
            } else if (sortOption.equals("Name (Z-A)")) {
                categories = categoryService.getAllSortedDesc();
            } else {
                categories = categoryService.getAll();
            }

            for (category c : categories) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/marketplaceManagement/category_item.fxml"));
                    Parent categoryItem = loader.load();

                    CategoryItemController itemController = loader.getController();
                    itemController.setData(c, this);

                    categoryContainer.getChildren().add(categoryItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/marketplaceManagement/category_form.fxml"));
            Parent formView = loader.load();

            CategoryController controller = loader.getController();
            controller.setListController(this);

            // Navigate to the form view
            showCategoryForm(formView);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open add category form: " + e.getMessage());
        }
    }

    /**
     * Shows the category list view in the content area
     */
    public void showCategoryList() {
        try {
            // Get the root BorderPane from the DashboardProductController
            DashboardProductController dashboard = getDashboardController();
            if (dashboard != null) {
                dashboard.loadView("/marketplaceManagement/category_list.fxml");
                return;
            }

            // If we cannot use the dashboard controller, try direct approach
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/marketplaceManagement/category_list.fxml"));
                Parent listView = loader.load();

                // Get the controller and set up references
                CategoryListController controller = loader.getController();
                if (dashboardController != null) {
                    controller.setDashboardController(dashboardController);
                }

                contentArea.getChildren().setAll(listView);
            } else {
                throw new Exception("Content area not found in scene graph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not return to category list: " + e.getMessage());
        }
    }

    /**
     * Shows the category form view in the content area
     * @param formView The loaded form view to display
     */
    public void showCategoryForm(Parent formView) {
        try {
            // First try to use the dashboard controller if available
            DashboardProductController dashboard = getDashboardController();
            if (dashboard != null && dashboard.getContentArea() != null) {
                dashboard.getContentArea().getChildren().setAll(formView);
                return;
            }

            // Next try using the FXML-injected content area
            if (this.contentArea != null) {
                this.contentArea.getChildren().setAll(formView);
                return;
            }

            // If those aren't available, try to find it through the scene graph
            if (categoryContainer != null && categoryContainer.getScene() != null) {
                StackPane contentArea = findContentArea();

                if (contentArea != null) {
                    contentArea.getChildren().setAll(formView);
                } else {
                    throw new Exception("Content area not found in scene graph");
                }
            } else {
                throw new Exception("No node available to access scene graph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to category form: " + e.getMessage());
        }
    }

    /**
     * Helper method to find the content area in the scene graph
     */
    private StackPane findContentArea() {
        if (categoryContainer == null || categoryContainer.getScene() == null) {
            return null;
        }

        Parent root = categoryContainer.getScene().getRoot();

        // First try lookup by ID
        StackPane contentArea = (StackPane) root.lookup("#contentArea");
        if (contentArea != null) {
            return contentArea;
        }

        // If that fails, recursively search through the scene graph
        return findContentAreaRecursive(root);
    }

    /**
     * Recursively searches for the contentArea StackPane in the scene graph
     */
    private StackPane findContentAreaRecursive(Parent node) {
        // First check if this node is the contentArea
        if (node instanceof StackPane && "contentArea".equals(node.getId())) {
            return (StackPane) node;
        }

        // Try to find in children
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                StackPane result = findContentAreaRecursive((Parent) child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public void setDashboardController(DashboardProductController controller) {
        this.dashboardController = controller;
    }

    private DashboardProductController getDashboardController() {
        // First check if we have a direct reference
        if (dashboardController != null) {
            return dashboardController;
        }

        // If not, try to find it through the scene graph
        try {
            if (categoryContainer == null || categoryContainer.getScene() == null) {
                return null;
            }

            Parent root = categoryContainer.getScene().getRoot();
            if (root instanceof BorderPane) {
                BorderPane dashboardRoot = (BorderPane) root;
                // Get controller using lookup ID
                if ("dashboardRoot".equals(dashboardRoot.getId())) {
                    // Get the controller through reflection or scene properties
                    return (DashboardProductController) dashboardRoot.getProperties().get("controller");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}