package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import services.marketPlace.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

import java.util.List;

public class MarketplaceViewController {

    @FXML private FlowPane productContainer; // Match the type in FXML (FlowPane)
    @FXML private BorderPane root;

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<product> products = productService.getAll();

            if (products == null || products.isEmpty()) {
                System.out.println("No products available to display");
                return;
            }

            productContainer.getChildren().clear();
            System.out.println("Loading " + products.size() + " products");

            for (product p : products) {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_card.fxml"));
                    Pane card = loader.load();

                    ProductCardController controller = loader.getController();
                    controller.setProductData(p, this);
                    //controller.setProductData(p, this);

                    productContainer.getChildren().add(card);
                    //System.out.println("Product card added to container: " + p.getNameProd());
            }
        } catch (Exception e) {
            System.err.println("Error in loadProducts method: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void addToCart(product p) {
        System.out.println("Added to cart: " + p.getNameProd());
    }

    @FXML
    private void handleNavigate(javafx.event.ActionEvent event) {
        System.out.println("Marketplace navigation button clicked.");
    }
}