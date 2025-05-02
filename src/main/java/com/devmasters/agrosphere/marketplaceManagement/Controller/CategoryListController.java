package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

// Fixed: Removed incorrect import of javax.swing.plaf.synth.Region

public class CategoryListController {

    @FXML public VBox categoryContainer;
    @FXML private TextField categorySearchField;
    // Fixed: Removed unused commented field
    @FXML private Button btnAddCategory;
    @FXML private ComboBox<String> categorySortCombo;

    private CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        loadCategories();

        // Setup search listener
        categorySearchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                performSearch(newValue);
            }
        });

        // Setup sort combo listener if it exists
        if (categorySortCombo != null) {
            categorySortCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    sortCategories(newValue);
                }
            });
        }
    }

    public void loadCategories() {
        try {
            categoryContainer.getChildren().clear();
            List<category> categories = categoryService.getAll();

            if (categories.isEmpty()) {
                Label noResultsLabel = new Label("No categories found");
                noResultsLabel.getStyleClass().add("no-results-label");
                categoryContainer.getChildren().add(noResultsLabel);
                return;
            }

            for (category c : categories) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/category_item.fxml"));
                HBox item = loader.load();

                CategoryItemController controller = loader.getController();
                controller.setData(c, this);

                categoryContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        try {
            // Fixed: Cast to Parent instead of Region since FXML loads a VBox
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagement/category_form.fxml"
            ));
            Parent formView = loader.load();

            // Get the controller and set the list controller reference
            CategoryController controller = loader.getController();
            controller.setListController(this);

            // Load into the StackPane with fx:id="contentArea"
            StackPane contentArea = (StackPane) categoryContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(formView);
            } else {
                throw new IOException("Content area not found in scene graph");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open category form: " + e.getMessage());
        }
    }

    // Added method to handle showing category list view when returning from form
    public void showCategoryList() {
        try {
            if (categoryContainer == null || categoryContainer.getScene() == null) {
                throw new NullPointerException("Container or scene is null");
            }

            // Get root and find contentArea more reliably
            javafx.scene.Parent root = categoryContainer.getScene().getRoot();
            StackPane contentArea = null;

            // Try to find contentArea through scene graph traversal
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
                if (borderPane.getCenter() instanceof StackPane) {
                    contentArea = (StackPane) borderPane.getCenter();
                }
            } else {
                // Fall back to lookup if direct traversal fails
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
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not return to category list: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Navigation components not available: " + e.getMessage());
        }
    }

    private void performSearch(String keyword) {
        try {
            List<category> results;

            if (keyword == null || keyword.trim().isEmpty()) {
                results = categoryService.getAll();
            } else {
                results = categoryService.searchByName(keyword);
            }

            categoryContainer.getChildren().clear();

            if (results.isEmpty()) {
                Label noResultsLabel = new Label("No categories found matching '" + keyword + "'");
                noResultsLabel.getStyleClass().add("no-results-label");
                categoryContainer.getChildren().add(noResultsLabel);
                return;
            }

            for (category c : results) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/category_item.fxml"));
                HBox item = loader.load();
                CategoryItemController controller = loader.getController();
                controller.setData(c, this);
                categoryContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Search Error",
                    "Failed to search categories: " + e.getMessage());
        }
    }

    private void sortCategories(String sortOption) {
        try {
            List<category> categories = categoryService.getAll();

            if (sortOption.equals("Name (A-Z)")) {
                categories.sort((c1, c2) -> c1.getNameCategory().compareToIgnoreCase(c2.getNameCategory()));
            } else if (sortOption.equals("Name (Z-A)")) {
                categories.sort((c1, c2) -> c2.getNameCategory().compareToIgnoreCase(c1.getNameCategory()));
            }

            categoryContainer.getChildren().clear();

            for (category c : categories) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/category_item.fxml"));
                HBox item = loader.load();
                CategoryItemController controller = loader.getController();
                controller.setData(c, this);
                categoryContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Sort Error",
                    "Failed to sort categories: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}