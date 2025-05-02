package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class CartViewController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTVA;
    @FXML private Label lblTotal;
    @FXML private Button btnPurchase;

    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();
    private final ProductService productService = new ProductService();

    private int buyerId = 3;
    private order currentOrder;

    @FXML
    public void initialize() {
        loadCart();

        // Set purchase button action
        btnPurchase.setOnAction(e -> completePurchase());
    }

    private void loadCart() {
        try {
            // Find current pending order
            currentOrder = findPendingOrder();

            if (currentOrder == null) {
                // No pending order found, display empty cart
                displayEmptyCart();
                return;
            }

            List<orderLine> lines = orderLineService.getByOrderId(currentOrder.getId());
            if (lines.isEmpty()) {
                displayEmptyCart();
                return;
            }

            // Clear container before adding items
            cartItemsContainer.getChildren().clear();

            double subtotal = 0;
            for (orderLine ol : lines) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/cart_item.fxml"));
                HBox item = loader.load();

                CartItemController controller = loader.getController();
                controller.setData(ol, this::loadCart); // Reload cart when item changes

                cartItemsContainer.getChildren().add(item);

                // Calculate line total and add to subtotal
                product prod = productService.getOne(ol.getProductId());
                double lineTotal = prod.getPriceProd() * ol.getOrderQuantity();
                subtotal += lineTotal;
            }

            System.out.println("🛒 Loading cart for pending order ID: " + currentOrder.getId());

            // Calculate tax and total
            double tva = subtotal * 0.07;
            double total = subtotal + tva;

            // Update labels with formatted values
            lblSubtotal.setText("Total price: " + String.format("%.2f", subtotal) + " TND");
            lblTVA.setText("TVA (7%): " + String.format("%.2f", tva) + " TND");
            lblTotal.setText("Total: " + String.format("%.2f", total) + " TND");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading cart", e.getMessage());
        }
    }

    /**
     * Helper method to find the current pending order
     */
    private order findPendingOrder() {
        try {
            List<order> all = orderService.getAll();
            for (order o : all) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    return o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Display an empty cart state
     */
    private void displayEmptyCart() {
        cartItemsContainer.getChildren().clear();

        // Set default values for empty cart
        lblSubtotal.setText("Total price: 0.00 TND");
        lblTVA.setText("TVA (7%): 0.00 TND");
        lblTotal.setText("Total: 0.00 TND");

        // Add empty cart message
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888; -fx-padding: 20px;");
        cartItemsContainer.getChildren().add(emptyLabel);

        // Disable purchase button
        btnPurchase.setDisable(true);
    }

    /**
     * Complete the purchase by changing order status to confirmed
     */
    private void completePurchase() {
        try {
            if (currentOrder == null) {
                showAlert("Error", "No active order found");
                return;
            }

            // Change order status to confirmed
            currentOrder.setStatus("confirmed");
            orderService.update(currentOrder);

            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Purchase Completed");
            alert.setHeaderText(null);
            alert.setContentText("Your order has been confirmed and is being processed!");
            alert.showAndWait();

            // Clear the cart display
            displayEmptyCart();

            // Go back to the marketplace
            goBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error completing purchase", e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_marketplace.fxml"));
            BorderPane shopView = loader.load();

            // Get the root BorderPane of the scene
            BorderPane root = (BorderPane) cartItemsContainer.getScene().getRoot();
            root.setCenter(shopView);

            // Get the controller and update the cart badge
            MarketplaceViewController controller = loader.getController();
            controller.updateCartBadge();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error navigating back", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}