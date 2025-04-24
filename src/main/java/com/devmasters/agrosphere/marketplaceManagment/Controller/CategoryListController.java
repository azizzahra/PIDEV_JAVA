package com.devmasters.agrosphere.marketplaceManagment.Controller;

import com.devmasters.agrosphere.marketplaceManagment.entities.category;
import com.devmasters.agrosphere.userManagament.entities.user;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.List;
public class CategoryListController {

    @FXML
    public VBox categoryContainer;

    private CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        loadCategories();
    }

    public void loadCategories() {
        try {
            categoryContainer.getChildren().clear();
            List<category> categories = categoryService.getAll();

            for (category c : categories) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/category_item.fxml"));
                HBox item = loader.load();

                CategoryItemController controller = loader.getController();
                controller.setData(c, this);

                categoryContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagment/category_form.fxml"
            ));
            Parent formRoot = loader.load();

            // Load the scene (or update the current center if it's inside a dashboard BorderPane)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(formRoot));
            stage.setTitle("Add New Category");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
