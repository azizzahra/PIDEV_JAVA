package com.devmasters.agrosphere.marketplaceManagment.Controller;
import com.devmasters.agrosphere.marketplaceManagment.entities.category;
import services.marketPlace.CategoryService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import java.util.Optional;
import javafx.scene.control.ButtonType;

public class CategoryItemController {

    @FXML private Label nameLabel;
    @FXML private Label descLabel;

    private category currentCategory;
    private CategoryListController listController;

    public void setData(category c, CategoryListController controller) {
        this.currentCategory = c;
        this.listController = controller;

        nameLabel.setText(c.getNameCategory());
        descLabel.setText(c.getDescriptionCategory());
    }

    @FXML
    private void onEditClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/category_form.fxml"));
            VBox formView = loader.load();

            CategoryController formController = loader.getController();
            formController.setCategoryToEdit(currentCategory);
            formController.setListController(listController);

            listController.categoryContainer.getChildren().clear();
            listController.categoryContainer.getChildren().add(formView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onDeleteClicked() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete category: " + currentCategory.getNameCategory());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CategoryService cs = new CategoryService();
                cs.delete(currentCategory.getId());

                listController.loadCategories();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("✅ Category deleted!");
                success.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

