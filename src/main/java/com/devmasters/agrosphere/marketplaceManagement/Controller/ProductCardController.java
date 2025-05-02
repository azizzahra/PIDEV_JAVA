package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import services.marketPlace.CategoryService;

import java.io.File;

public class ProductCardController {

    @FXML
    private VBox productCard;

    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label stockLabel;
    @FXML private Button categoryButton;
    @FXML private ImageView imageView;
    private MarketplaceViewController listController;

    @FXML
    private Button viewButton;
    @FXML
    private Button addToCartButton;

    @FXML
    public void initialize() {
        // Set default button actions
        viewButton.setOnAction(event -> handleViewProduct());
        addToCartButton.setOnAction(event -> handleAddToCart());

        // Make sure the description is not too long by trimming it
        if (descriptionLabel.getText().length() > 80) {
            descriptionLabel.setText(descriptionLabel.getText().substring(0, 77) + "...");
        }
    }

    public void setProductData(product p, MarketplaceViewController controller) {
        this.listController = controller;

        nameLabel.setText(p.getNameProd());

        // Trim description if too long
        String description = p.getDescriptionProd();
        if (description != null && description.length() > 80) {
            description = description.substring(0, 77) + "...";
        }
        descriptionLabel.setText(description);

        priceLabel.setText(p.getPriceProd() + " TND");
        stockLabel.setText("Stock: " + p.getQuantity());

        // Load category name instead of ID
        CategoryService cs = new CategoryService();
        try {
            category c = cs.getOne(p.getCategoryProdId());
            categoryButton.setText(c.getNameCategory());
        } catch (Exception e) {
            categoryButton.setText("Unknown");
        }

        // Load and set image with proper error handling
        try {
            File imageFile = new File("uploads/" + p.getProdImg());
            if (imageFile.exists()) {
                Image img = new Image(imageFile.toURI().toString(), true); // true for background loading
                imageView.setImage(img);

                // Center the image and make sure it's displayed properly
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
            } else {
                // Load a default placeholder image if product image doesn't exist
                Image placeholderImage = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
                if (placeholderImage != null) {
                    imageView.setImage(placeholderImage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image for product: " + p.getNameProd());
            e.printStackTrace();
        }
    }

    /**
     * Handle view product action
     */
    private void handleViewProduct() {
        System.out.println("View product: " + nameLabel.getText());
        // Implement view product logic here
    }

    /**
     * Handle add to cart action
     */
    private void handleAddToCart() {
        System.out.println("Adding to cart: " + nameLabel.getText());
        // Implement add to cart logic here
    }
}