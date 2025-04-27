package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import services.marketPlace.OrderLineService;
import services.marketPlace.ProductService;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderItemController {

    @FXML private Label lblOrderHeader;
    @FXML private Label lblOrderStatus;
    @FXML private Label lblTotalPrice;
    @FXML private VBox orderLinesContainer;
    @FXML private Button btnProcessOrder;
    @FXML private Button btnViewDetails;

    private final OrderLineService orderLineService = new OrderLineService();
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();
    private order currentOrder;

    public void setData(order o) {
        this.currentOrder = o;

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String formattedPrice = currencyFormat.format(o.getTotalPrice());

        // Get buyer name from user service
        String buyerName = getUserName(o.getBuyerId());
        lblOrderHeader.setText(buyerName);

        // Set status with appropriate style
        lblOrderStatus.setText("Status: " + o.getStatus());
        String statusClass = getStatusStyleClass(o.getStatus());
        lblOrderStatus.getStyleClass().clear();
        lblOrderStatus.getStyleClass().add(statusClass);

        // Set total price in right column
        lblTotalPrice.setText("Total: " + formattedPrice);

        // Load order lines
        loadOrderLines(o.getId());
    }

    // Updated to use the userService.getOne method
    private String getUserName(int userId) {
        try {
            // Use the UserService to get the user name
            try {
                com.devmasters.agrosphere.userManagament.entities.user user = userService.getOne(userId);
                if (user != null) {
                    return user.getPrenom() + " " + user.getNom();
                }
            } catch (Exception e) {
                System.err.println("Error fetching user: " + e.getMessage());
            }
            return "Unknown User";
        } catch (Exception e) {
            return "Unknown User";
        }
    }

    private void loadOrderLines(int orderId) {
        // Clear previous items
        orderLinesContainer.getChildren().clear();

        try {
            List<orderLine> lines = orderLineService.getByOrderId(orderId);

            for (orderLine ol : lines) {
                HBox lineBox = new HBox(10);
                lineBox.setAlignment(Pos.CENTER_LEFT);
                lineBox.setPadding(new Insets(5, 0, 5, 15));

                // Get product name from the ProductService
                String productName = productService.getProductName(ol.getProductId());

                // Use actual product name instead of ID
                Label productLabel = new Label("📦 " + productName);
                Label quantityLabel = new Label("Quantity: " + ol.getOrderQuantity());
                quantityLabel.setStyle("-fx-font-weight: bold;");

                lineBox.getChildren().addAll(productLabel, quantityLabel);
                lineBox.getStyleClass().add("order-item");

                orderLinesContainer.getChildren().add(lineBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Could not load order details: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            orderLinesContainer.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void handleProcessOrder(ActionEvent event) {
        // Implement order processing logic here
        System.out.println("Processing order #" + currentOrder.getId());
        // You might want to call a service method to update the order status
        // and then refresh the UI
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        // Implement view details logic here
        System.out.println("Viewing details for order #" + currentOrder.getId());
        // You might want to open a new window/dialog with detailed information
    }

    private String getStatusStyleClass(String status) {
        if (status == null) return "order-status-waiting";

        switch (status.toLowerCase()) {
            case "completed":
                return "order-status-completed";
            case "cancelled":
                return "order-status-cancelled";
            case "on wait":
            case "waiting":
            case "pending":
            default:
                return "order-status-waiting";
        }
    }
}