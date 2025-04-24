package com.devmasters.agrosphere.marketplaceManagment.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

import java.io.IOException;

public class DashboardProductController {

    @FXML
    private StackPane contentArea;
    @FXML
    private BorderPane dashboardRoot;


    @FXML
    public void initialize() {
        loadView("/com/devmasters/agrosphere/marketplaceManagment/product_list.fxml");
    }

    /*public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Region view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProductList() {
        loadView("/com/devmasters/agrosphere/marketplaceManagment/product_list.fxml");
    }

    @FXML
    private BorderPane mainLayout;
    /*@FXML
    public void handleAddProduct() {
        try {
            Region form = FXMLLoader.load(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagment/product_form.fxml"
            ));
            mainLayout.setCenter(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    private BorderPane root; // fx:id="root" in DashboardProduct.fxml

    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/product_form.fxml"));
            Region formView = loader.load();
            root.setCenter(formView); // just update center
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoryList() {
        loadView("/com/devmasters/agrosphere/marketplaceManagment/category_list.fxml");
    }

    @FXML
    private void handleDashboard() {
        // Optionally load a home screen/dashboard
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        // Implement actual logout logic here
    }
    @FXML
    private void handleOrdersList() {
        // This will be implemented later when you build the orders functionality
        System.out.println("Orders list requested - feature coming soon");
        // For now, you can show a placeholder or message
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/placeholder.fxml"));
            Region view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArchivedProducts() {
        // This will be implemented later when you build the archive functionality
        System.out.println("Archived products requested - feature coming soon");
        // For now, you can show a placeholder or message
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/placeholder.fxml"));
            Region view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
