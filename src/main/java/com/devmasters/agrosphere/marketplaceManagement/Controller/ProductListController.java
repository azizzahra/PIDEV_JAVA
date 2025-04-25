package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import services.marketPlace.ProductService;
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

    public void loadProducts() {
        try {
            productContainer.getChildren().clear();

            List<product> products = productService.getAll();
            for (product p : products) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_item.fxml"));
                AnchorPane productItem = loader.load();

                ProductItemController controller = loader.getController();
                controller.setData(p, this);

                productContainer.getChildren().add(productItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private StackPane contentArea;

    public void editProduct(product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/devmasters/agrosphere/marketplaceManagement/product_form.fxml"
            ));
            Region formView = loader.load();

            // Inject the product into the form
            ProductController controller = loader.getController();
            controller.setProductToEdit(p);

            // Find the parent StackPane and replace its content
            StackPane contentArea = (StackPane) productContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(formView);
            }
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


    @FXML
    private void handleCategoryNav() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/category_list.fxml"));
            Region view = loader.load();
            root.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_form.fxml"));
            Region formView = loader.load();

            StackPane contentArea = (StackPane) productContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(formView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BorderPane getParentBorderPane() {
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
