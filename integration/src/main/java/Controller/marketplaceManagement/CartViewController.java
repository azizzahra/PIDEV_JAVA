package Controller.marketplaceManagement;

import model.order;
import model.orderLine;
import model.product;
import model.user;
import com.stripe.exception.StripeException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.marketPlace.OrderLineService;
import services.marketPlace.OrderService;
import services.marketPlace.ProductService;
import services.marketPlace.StripePaymentService;
import services.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class CartViewController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTVA;
    @FXML private Label lblTotal;
    @FXML private Button btnPurchase;
    @FXML private Button btninvoice;

    private final OrderService orderService = new OrderService();
    private final OrderLineService orderLineService = new OrderLineService();
    private final ProductService productService = new ProductService();
    private final StripePaymentService stripePaymentService = new StripePaymentService();

    private MarketplaceViewController marketplaceController;
    private OrderHistoryViewController OrderHistory;
    private int buyerId; // Changed from final to allow initialization in initialize()
    private order currentOrder;
    private double currentTotal;

    @FXML
    public void initialize() {
        // Use the same buyer ID retrieval logic as MarketplaceViewController
        buyerId = getBuyerId();
        loadCartItems();
        btnPurchase.setOnAction(e -> processPurchase());
    }

    // Add the same getBuyerId method as in MarketplaceViewController
    private int getBuyerId() {
        // First try to get the user from the services.SessionManager
        user currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getId();
        }

        // Fallback to the Controller.SessionManager if needed
        return Controller.SessionManager.getInstance().getUserFront();
    }

    public void setMarketplaceController(MarketplaceViewController controller) {
        this.marketplaceController = controller;
    }
    public void setOrderHistory(OrderHistoryViewController controller) {
        this.OrderHistory = controller;
    }

    private void loadCartItems() {
        try {
            cartItemsContainer.getChildren().clear();
            currentOrder = findPendingOrder();

            if (currentOrder != null) {
                List<orderLine> orderLines = orderLineService.getByOrderId(currentOrder.getId());

                if (orderLines.isEmpty()) {
                    showEmptyCart();
                } else {
                    loadCartItems(orderLines);
                }
                updateTotals();
            } else {
                showEmptyCart();
                resetTotals();
            }
        } catch (Exception e) {
            handleError("Error loading cart items", e);
        }
    }

    private void loadCartItems(List<orderLine> orderLines) throws Exception {
        for (orderLine line : orderLines) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/cart_item.fxml"));
            javafx.scene.layout.HBox cartItemView = loader.load();
            CartItemController controller = loader.getController();
            controller.setData(line, this::updateTotals);
            cartItemsContainer.getChildren().add(cartItemView);
        }
    }

    public void updateTotals() {
        try {
            if (currentOrder == null) return;

            List<orderLine> lines = orderLineService.getByOrderId(currentOrder.getId());
            double subtotal = calculateSubtotal(lines);
            double tax = subtotal * 0.17;
            currentTotal = subtotal + tax;

            updateLabels(subtotal, tax, currentTotal);

            if (lines.isEmpty()) loadCartItems();
        } catch (Exception e) {
            handleError("Error updating totals", e);
        }
    }

    private double calculateSubtotal(List<orderLine> lines) throws Exception {
        double subtotal = 0.0;
        for (orderLine line : lines) {
            product prod = productService.getOne(line.getProductId());
            subtotal += prod.getPriceProd() * line.getOrderQuantity();
        }
        return subtotal;
    }

    private void updateLabels(double subtotal, double tax, double total) {
        double exchangeRate = 0.33; // Remplacer par un vrai taux
        double totalUSD = total * exchangeRate;

        lblSubtotal.setText(String.format("Total price: %.2f TND (≈%.2f USD)", subtotal, subtotal * exchangeRate));
        lblTVA.setText(String.format("TVA (17%%): %.2f TND", tax));
        lblTotal.setText(String.format("Total: %.2f TND (≈%.2f USD)", total, totalUSD));
    }


    private void processPurchase() {
        try {
            validateCart();

            // Mise à jour du total_price dans l'objet order avant le paiement
            updateOrderTotalPrice();

            handleStripePayment();
        } catch (EmptyCartException e) {
            showAlert("Empty Cart", e.getMessage());
        } catch (Exception e) {
            handleError("Payment Error", e);
        }
    }
    private void updateOrderTotalPrice() throws Exception {
        if (currentOrder != null) {
            // Recalcule les totaux pour s'assurer d'avoir les valeurs les plus récentes
            updateTotals();

            // Met à jour le total_price dans l'objet order
            currentOrder.setTotalPrice(currentTotal);

            // Persiste la mise à jour dans la base de données
            orderService.update(currentOrder);
        }
    }

    private void validateCart() throws Exception {
        if (currentOrder == null || orderLineService.getByOrderId(currentOrder.getId()).isEmpty()) {
            throw new EmptyCartException("Your cart is empty. Please add products before purchasing.");
        }
    }

    private void handleStripePayment() {
        try {
            updateTotals();

            String successUrl = "http://localhost/payment-success?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = "http://localhost/payment-cancel";

            String checkoutUrl = stripePaymentService.createCheckoutSession(
                    currentOrder,
                    currentTotal,
                    successUrl,
                    cancelUrl
            );

            showPaymentWindow(checkoutUrl);

        } catch (StripeException e) {
            handleError("Stripe API Error", e);
        }
    }

    private void showPaymentWindow(String checkoutUrl) {
        Platform.runLater(() -> {
            Stage paymentStage = new Stage();
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            webEngine.locationProperty().addListener((obs, oldVal, newVal) ->
                    handleUrlChange(newVal, paymentStage));

            webEngine.load(checkoutUrl);
            paymentStage.setScene(new Scene(webView, 800, 600));
            paymentStage.setTitle("Secure Payment Gateway");
            paymentStage.show();
        });
    }

    private void handleUrlChange(String newVal, Stage paymentStage) {
        if (newVal != null) {
            if (newVal.startsWith("http://localhost/payment-success")) {
                handleSuccessfulPayment(newVal, paymentStage);
            } else if (newVal.startsWith("http://localhost/payment-cancel")) {
                handleCancelledPayment(paymentStage);
            }
        }
    }

    private void handleSuccessfulPayment(String url, Stage paymentStage) {
        try {
            String sessionId = extractSessionId(url);
            boolean paymentVerified = stripePaymentService.verifyPayment(sessionId);

            paymentStage.close();

            if (paymentVerified) {
                completeOrder();
                Platform.runLater(() -> {
                    showAlert("Payment Successful", "Thank you for your purchase!");
                    goBack();
                });
            } else {
                Platform.runLater(() ->
                        showAlert("Payment Failed", "Payment verification failed"));
            }
        } catch (Exception e) {
            handleError("Payment Verification Error", e);
        }
    }

    private String extractSessionId(String url) {
        return url.split("session_id=")[1];
    }

    private void completeOrder() throws SQLException {
        currentOrder.setStatus("completed");
        orderService.update(currentOrder);
    }

    private void handleCancelledPayment(Stage paymentStage) {
        paymentStage.close();
        Platform.runLater(() ->
                showAlert("Payment Cancelled", "Your payment was cancelled"));
    }

    // Helper methods
    private void showEmptyCart() {
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888;");
        cartItemsContainer.getChildren().add(emptyLabel);
    }

    private void resetTotals() {
        lblSubtotal.setText("Total price: 0 TND");
        lblTVA.setText("TVA (17%): 0.0 TND");
        lblTotal.setText("Total: 0.0 TND");
    }

    @FXML
    public void goBack() {
        if (marketplaceController != null) {
            marketplaceController.loadMarketplaceView();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplaceManagement/product_marketplace.fxml"));
                Parent marketplaceView = loader.load();
                cartItemsContainer.getScene().setRoot(marketplaceView);
            } catch (Exception e) {
                handleError("Navigation Error", e);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleError(String context, Exception e) {
        System.err.println(context + ": " + e.getMessage());
        e.printStackTrace();
        showAlert("Error", context + ": " + e.getMessage());
    }

    private order findPendingOrder() {
        try {
            // Debug output to help diagnose the issue
            System.out.println("Looking for pending order for buyer ID: " + buyerId);

            List<order> allOrders = orderService.getAll();
            System.out.println("Total orders in system: " + allOrders.size());

            order pendingOrder = allOrders.stream()
                    .filter(o -> o.getBuyerId() == buyerId && "pending".equals(o.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (pendingOrder != null) {
                System.out.println("Found pending order with ID: " + pendingOrder.getId());
            } else {
                System.out.println("No pending order found for buyer ID: " + buyerId);
            }

            return pendingOrder;
        } catch (Exception e) {
            handleError("Order Search Error", e);
            return null;
        }
    }

    private static class EmptyCartException extends Exception {
        public EmptyCartException(String message) {
            super(message);
        }
    }
}