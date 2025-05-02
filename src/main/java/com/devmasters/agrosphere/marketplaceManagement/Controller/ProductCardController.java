package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.category;
import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import services.marketPlace.CategoryService;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;

import java.io.File;
import java.util.List;

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
    private product currentProduct;

    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();

    @FXML
    private Button viewButton;
    @FXML
    private Button addToCartButton;

    // Default buyer ID (in a real app, this would come from a logged in user)
    private int buyerId = 3;

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
        this.currentProduct = p;

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
     * Handle add to cart action - this is the main function we're implementing
     */
    private void handleAddToCart() {
        try {
            // 1. Find if there's an existing pending order for this buyer
            order currentOrder = null;
            List<order> allOrders = orderService.getAll();

            for (order o : allOrders) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    currentOrder = o;
                    break;
                }
            }

            // 2. If no pending order exists, create a new one
            if (currentOrder == null) {
                currentOrder = new order();
                currentOrder.setBuyerId(buyerId);
                currentOrder.setStatus("pending");
                int newOrderId = orderService.add(currentOrder);
                currentOrder.setId(newOrderId);
                System.out.println("Created new order with ID: " + newOrderId);
            }

            // 3. Check if the product is already in the cart
            boolean productExists = false;
            List<orderLine> existingLines = orderLineService.getByOrderId(currentOrder.getId());

            for (orderLine line : existingLines) {
                if (line.getProductId() == currentProduct.getId()) {
                    // Product already in cart, increase quantity
                    line.setOrderQuantity(line.getOrderQuantity() + 1);
                    orderLineService.update(line);
                    productExists = true;
                    System.out.println("Updated quantity for product: " + currentProduct.getNameProd());
                    break;
                }
            }

            // 4. If product not in cart, add it as a new line
            if (!productExists) {
                orderLine newLine = new orderLine();
                newLine.setOrdId(currentOrder.getId());
                newLine.setProductId(currentProduct.getId());
                newLine.setOrderQuantity(1);
                orderLineService.add(newLine);
                System.out.println("Added new product to cart: " + currentProduct.getNameProd());
            }

            // 5. Show success message or notification
            System.out.println("Added to cart: " + nameLabel.getText());

            // 6. Update cart badge count if you have one
            if (listController != null) {
                listController.updateCartBadge();
            }

        } catch (Exception e) {
            System.err.println("Error adding product to cart");
            e.printStackTrace();
        }
    }
}