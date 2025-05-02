package com.devmasters.agrosphere.marketplaceManagement.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;

public class loginController {

    @FXML
    private void handleFarmerView(ActionEvent event) {
        loadScene("/com/devmasters/agrosphere/marketplaceManagement/DashboardProduct.fxml",
                "/com/devmasters/agrosphere/marketplaceManagement/assets/css/modern.css",
                "AgroSphere - Farmer Dashboard",
                event);
    }

    @FXML
    private void handleBuyerView(ActionEvent event) {
        loadScene("/com/devmasters/agrosphere/marketplaceManagement/product_marketplace.fxml",
                "/com/devmasters/agrosphere/marketplaceManagement/assets/css/marketplace.css", // Changed to marketplace.css
                "AgroSphere - Marketplace",
                event);
    }

    private void loadScene(String fxmlPath, String cssPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1546, 777);

            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Warning: CSS file not found at: " + cssPath);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

            // Close login window - getting the current stage from the event
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            System.err.println("Error loading scene: " + fxmlPath);
            e.printStackTrace();
        }
    }
}