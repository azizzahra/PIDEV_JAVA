package Controller.marketplaceManagement;

import model.order;
import model.orderLine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import services.UserService;

import java.sql.SQLException;
import java.util.List;

public class OrderItemController {

    @FXML private Label orderIdLabel;
    @FXML private Label buyerNameLabel;
    @FXML private Label orderTotalLabel;
    @FXML private Label orderStatusLabel;
    @FXML private Button btnViewDetails;
    @FXML private Button btnDeliver;
    @FXML private VBox itemsContainer;

    private order currentOrder;
    private OrderService orderService;
    private final OrderLineService orderLineService = new OrderLineService();
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();

    public void setData(order order, OrderService service) {
        this.currentOrder = order;
        this.orderService = service;

        // Display order information
        if (orderIdLabel != null)
            orderIdLabel.setText("Order #" + order.getId());


        String buyerName = getUserName(order.getBuyerId());
        buyerNameLabel.setText(buyerName);

        if (orderTotalLabel != null)
            orderTotalLabel.setText(String.format("%.2f TND", order.getTotalPrice()));

        // Set status and color
        updateStatusDisplay(order.getStatus());

        // Populate items with quantity
        loadOrderLines(order.getId());

        // Setup deliver button
        if (btnDeliver != null) {
            // Hide the deliver button if already delivered
            if (order.getStatus().equalsIgnoreCase("delivered") ||
                    order.getStatus().equalsIgnoreCase("delivred") ||
                    order.getStatus().equalsIgnoreCase("cancelled")) {
                btnDeliver.setDisable(true);
                btnDeliver.setVisible(false);
            } else {
                btnDeliver.setOnAction(event -> deliverOrder());
            }
        }

        // Setup view details button
        if (btnViewDetails != null) {
            btnViewDetails.setOnAction(event -> viewOrderDetails());
        }
    }

    // Legacy support for old method signature
    public void setData(order order) {
        setData(order, null);
    }
    private String getUserName(int userId) {
        try {
            // Use the UserService to get the user name
            try {
                model.user user = userService.getOne(userId);
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
    private void updateStatusDisplay(String status) {
        if (orderStatusLabel != null) {
            orderStatusLabel.setText(status);

            // Set status color
            String statusLower = status.toLowerCase();
            String textColor;

            if (statusLower.equals("delivered") || statusLower.equals("delivred")) {
                textColor = "#2e7d32"; // Green
            } else if (statusLower.equals("pending")) {
                textColor = "#f57c00"; // Orange
            } else if (statusLower.equals("processing") || statusLower.equals("on wait")) {
                textColor = "#1976d2"; // Blue
            } else if (statusLower.equals("cancelled")) {
                textColor = "#d32f2f"; // Red
            } else {
                textColor = "#616161"; // Gray
            }

            orderStatusLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        }
    }

    private void loadOrderLines(int orderId) {
        // Clear previous items
        itemsContainer.getChildren().clear();

        try {
            List<orderLine> lines = orderLineService.getByOrderId(orderId);

            for (orderLine ol : lines) {
                HBox lineBox = new HBox(10);
                lineBox.setAlignment(Pos.CENTER_LEFT);
                lineBox.setPadding(new Insets(5, 0, 5, 15));

                // Get product name from the ProductService
                String productName = productService.getProductName(ol.getProductId());

                // Use actual product name instead of ID
                Label productLabel = new Label( productName);
                Label quantityLabel = new Label("Quantity: " + ol.getOrderQuantity());
                quantityLabel.setStyle("-fx-font-weight: bold;");

                lineBox.getChildren().addAll(productLabel, quantityLabel);
                lineBox.getStyleClass().add("order-item");

                itemsContainer.getChildren().add(lineBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Could not load order details: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            itemsContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createItemRow(String itemName, int quantity) {
        HBox row = new HBox(10);

        Label nameLabel = new Label(itemName);
        nameLabel.setStyle("-fx-font-weight: normal;");
        nameLabel.setPrefWidth(150);

        Label qtyLabel = new Label("Quantity: " + quantity);
        qtyLabel.setStyle("-fx-text-fill: #64748b;");

        row.getChildren().addAll(nameLabel, qtyLabel);
        return row;
    }

    private void deliverOrder() {
        if (orderService != null) {
            try {
                boolean updated = orderService.updateOrderStatus(currentOrder.getId(), "Delivered");

                if (updated) {
                    // Update the status label
                    updateStatusDisplay("Delivered");

                    // Update the current order object
                    currentOrder.setStatus("Delivered");

                    // Disable the deliver button
                    btnDeliver.setDisable(true);
                    btnDeliver.setVisible(false);

                    // Show confirmation tooltip
                    Tooltip tooltip = new Tooltip("Order marked as delivered");
                    tooltip.setAutoHide(true);
                    tooltip.show(btnDeliver,
                            btnDeliver.localToScreen(btnDeliver.getBoundsInLocal()).getMinX(),
                            btnDeliver.localToScreen(btnDeliver.getBoundsInLocal()).getMaxY());

                    // Hide tooltip after 2 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> tooltip.hide());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error updating order status: " + e.getMessage());
            }
        }
    }

    private void viewOrderDetails() {
        // Implementation for viewing order details
        System.out.println("Viewing details for order #" + currentOrder.getId());

        // Keep the existing implementation or placeholder as is
    }
}