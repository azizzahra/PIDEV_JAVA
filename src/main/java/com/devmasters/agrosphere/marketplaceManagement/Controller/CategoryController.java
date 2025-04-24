package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.Controller.CategoryListController;
import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import services.marketPlace.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoryController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    private category currentCategory;
    private CategoryService categoryService = new CategoryService();
    private CategoryListController listController;

    public void setCategoryToEdit(category c) {
        this.currentCategory = c;
        nameField.setText(c.getNameCategory());
        descriptionField.setText(c.getDescriptionCategory());
    }

    public void setListController(CategoryListController controller) {
        this.listController = controller;
    }

    @FXML
    private void saveCategory() {
        if (!validateCategoryInputs()) return;
        try {
            category c = (currentCategory == null) ? new category() : currentCategory;
            c.setNameCategory(nameField.getText());
            c.setDescriptionCategory(descriptionField.getText());

            if (currentCategory == null) {
                categoryService.add(c);
            } else {
                categoryService.update(c);
            }

            listController.loadCategories();
        } catch (Exception e) {
            e.printStackTrace();
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

