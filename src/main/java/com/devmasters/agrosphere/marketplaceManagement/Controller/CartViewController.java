package com.devmasters.agrosphere.marketplaceManagement.Controller;

import com.devmasters.agrosphere.marketplaceManagement.entities.order;
import com.devmasters.agrosphere.marketplaceManagement.entities.orderLine;
import com.devmasters.agrosphere.marketplaceManagement.entities.product;
import com.devmasters.agrosphere.userManagement.entities.user;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.user.UserService;

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

    private final UserService userService = new UserService(); // Add UserService

    private int buyerId = 3;
    private order currentOrder;

    @FXML
    public void initialize() {
        loadCart();
    }

    private void loadCart() {
        try {
            List<order> all = orderService.getAll();
            for (order o : all) {
                if (o.getBuyerId() == buyerId && o.getStatus().equals("pending")) {
                    currentOrder = o;
                    break;
                }
            }

            if (currentOrder == null) return;

            List<orderLine> lines = orderLineService.getByOrderId(currentOrder.getId());
            cartItemsContainer.getChildren().clear();

            double subtotal = 0;
            for (orderLine ol : lines) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/cart_item.fxml"));
                HBox item = loader.load();

                CartItemController controller = loader.getController();
                controller.setData(ol, this::loadCart); // 💡 triggers full cart reload on update

                cartItemsContainer.getChildren().add(item);

                double lineTotal = productService.getOne(ol.getProductId()).getPriceProd() * ol.getOrderQuantity();
                subtotal += lineTotal;
            }

            System.out.println("🛒 Loading cart for pending order ID: " + currentOrder.getId());

            double tva = subtotal * 0.07;
            double total = subtotal + tva;

            lblSubtotal.setText("Total price: " + subtotal + " TND");
            lblTVA.setText("TVA (7%): " + String.format("%.2f", tva) + " TND");
            lblTotal.setText("Total: " + String.format("%.2f", total) + " TND");

            /*btnPurchase.setOnAction(e -> {
                try {
                    currentOrder.setStatus("confirmed");
                    orderService.update(currentOrder);
                    cartItemsContainer.getChildren().clear();
                    lblTotal.setText("Total: 0.0 TND");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });*/
            btnPurchase.setOnAction(e -> {
                try {
                    // Confirm the order
                    currentOrder.setStatus("confirmed");
                    orderService.update(currentOrder);

                    // Show success alert
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Order Confirmed");
                    alert.setHeaderText(null);
                    alert.setContentText("✅ Your order has been successfully placed!");
                    alert.showAndWait(); // Wait for user to press OK

                    // Open invoice view
                    showInvoice(currentOrder);

                    // Clear UI
                    cartItemsContainer.getChildren().clear();
                    lblTotal.setText("Total: 0.0 TND");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setContentText("Failed to complete the order.");
                    error.showAndWait();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/product_marketplace.fxml"));
            Pane shopView = loader.load();

            // We need access to the root BorderPane of the original scene
            BorderPane root = (BorderPane) cartItemsContainer.getScene().lookup("#root"); // ID from your BorderPane
            root.setCenter(shopView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showInvoice(order order) {
        try {
            // Load order lines for this order
            List<orderLine> orderLines = orderLineService.getByOrderId(order.getId());

            // Get buyer information
            user buyer = userService.getOne(order.getBuyerId());

            // Get first product to find the farmer
            if (!orderLines.isEmpty()) {
                orderLine firstLine = orderLines.get(0);
                product prod = productService.getOne(firstLine.getProductId());

                // Get farmer information
                user farmer = userService.getOne(prod.getFarmerId());

                // Create invoice view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/devmasters/agrosphere/marketplaceManagement/invoice_view.fxml"));
                BorderPane invoiceView = loader.load();

                // Get the controller and set data
                InvoiceViewController controller = loader.getController();
                controller.setOrderData(
                        order,
                        orderLines,
                        buyer.getPrenom(),
                        buyer.getNom(),
                        buyer.getNumTel(),
                        buyer.getMail(),
                        farmer.getPrenom() + " " + farmer.getNom(),
                        "Farm " + farmer.getPrenom(), // Example farm name
                        farmer.getNumTel()
                );

                // Create a new stage for the invoice
                Stage invoiceStage = new Stage();
                invoiceStage.setTitle("Invoice #" + order.getId());
                invoiceStage.setScene(new Scene(invoiceView));
                invoiceStage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
