package com.devmasters.agrosphere.marketplaceManagement.Controller;


//import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardProductController {

    @FXML
    private StackPane contentArea;
    @FXML
    private BorderPane dashboardRoot;
    @FXML private BorderPane root;
    @FXML private Button btnProducts;
    @FXML private Button btnCategories;
    @FXML private Button btnOrders;
    @FXML private Button btnArchived;

    @FXML
    public void initialize() {
        // Load product list by default
        loadView("/com/devmasters/agrosphere/marketplaceManagement/product_list.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            //setActiveButton(btnProducts);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Region view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProductList() {
        setActiveButton(btnProducts);
        loadView("/com/devmasters/agrosphere/marketplaceManagement/product_list.fxml");
    }

    @FXML
    private void handleArchivedProducts() {
        setActiveButton(btnArchived);
        // This will be implemented later when you build the archive functionality
        System.out.println("Archived products requested - feature coming soon");
        loadView("/com/devmasters/agrosphere/marketplaceManagement/Archived.fxml");
    }


    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        // Implement actual logout logic here
    }
    @FXML
    private void handleOrdersList() {
        setActiveButton(btnOrders);
        try {
            VBox placeholder = new VBox();
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            placeholder.setSpacing(20);
            placeholder.setStyle("-fx-padding: 30;");

            Label title = new Label("Orders Feature Coming Soon");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitle = new Label("This feature is currently under development.");
            subtitle.setStyle("-fx-font-size: 16px;");

            placeholder.getChildren().addAll(title, subtitle);

            // Use contentArea instead of root
            contentArea.getChildren().setAll(placeholder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoryList() {
        setActiveButton(btnCategories);
        loadView("/com/devmasters/agrosphere/marketplaceManagment/category_list.fxml");
    }

    private void setActiveButton(Button activeButton) {
        List<Button> allButtons = List.of(btnProducts, btnCategories, btnOrders, btnArchived);
        for (Button b : allButtons) {
            b.getStyleClass().remove("product-nav-button-active");
        }
        activeButton.getStyleClass().add("product-nav-button-active");
    }
}