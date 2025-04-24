package com.devmasters.agrosphere.marketplaceManagment.Controller;

import com.devmasters.agrosphere.marketplaceManagment.entities.product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.marketPlace.ProductService;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.event.ActionEvent;
import java.io.IOException;

import java.util.List;

public class ProductListController {

    @FXML private VBox productContainer;
    @FXML private BorderPane root;
    @FXML private Button btnToggleView;
    private boolean inFormView = false;

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadProducts();
    }

    public  void loadProducts() {
        try {
            productContainer.getChildren().clear();

            List<product> products = productService.getAll();
            for (product p : products) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/product_item.fxml"));
                AnchorPane productItem = loader.load();

                ProductItemController controller = loader.getController();
                controller.setData(p, this);

                productContainer.getChildren().add(productItem);
            }
        } catch (Exception e) {
            e.printStackTrace(); // You can replace with logger if preferred
        }
    }

    public void editProduct(product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagment/product_form.fxml"
            ));
            Region formView = loader.load();

            // Inject the product into the form
            ProductController controller = loader.getController();
            controller.setProductToEdit(p);

            root.setCenter(formView); // Show form in center area of DashboardProduct
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void deleteProduct(int id) {
        try {
            ProductService ps = new ProductService();
            ps.delete(id);
        } catch (Exception e) { // Catch the generic Exception, not just IOException
            e.printStackTrace();
        }
    }



    /*@FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/product_form.fxml"));
            Region formView = loader.load();
            root.setCenter(formView); // just update center
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    //23.04.25

    @FXML
    private void handleCategoryNav() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/category_list.fxml"));
            Region view = loader.load();
            root.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOrdersNav() {
        // Placeholder for orders feature
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
            root.setCenter(placeholder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArchivedNav() {
        // Placeholder for archived products feature
        try {
            VBox placeholder = new VBox();
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            placeholder.setSpacing(20);
            placeholder.setStyle("-fx-padding: 30;");

            Label title = new Label("Archived Products Feature Coming Soon");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label subtitle = new Label("This feature is currently under development.");
            subtitle.setStyle("-fx-font-size: 16px;");

            placeholder.getChildren().addAll(title, subtitle);
            root.setCenter(placeholder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagment/product_form.fxml"));
            Parent formView = loader.load();

            // Find the dashboard root BorderPane
            BorderPane dashboardRoot = (BorderPane) getParentBorderPane();
            if (dashboardRoot != null) {
                dashboardRoot.setCenter(formView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BorderPane getParentBorderPane() {
        // Walk up the scene graph to find the dashboard root BorderPane
        Parent parent = productContainer.getParent();
        while (parent != null) {
            if (parent instanceof BorderPane && ((BorderPane) parent).getId() != null &&
                    ((BorderPane) parent).getId().equals("dashboardRoot")) {
                return (BorderPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}
