package Controller.marketplaceManagement;

import model.order;
import com.stripe.exception.StripeException;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.marketPlace.OrderService;
import services.marketPlace.StripePaymentService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentViewController {

    @FXML private Label lblOrderId;
    @FXML private Label lblAmount;
    @FXML private RadioButton rbCreditCard;
    @FXML private RadioButton rbPayAtDelivery;
    @FXML private VBox creditCardForm;
    @FXML private VBox payAtDeliveryForm;
    @FXML private StackPane paymentContainer;
    @FXML private Button btnPayWithStripe;
    @FXML private WebView webViewStripe;
    @FXML private HBox loadingIndicator;
    @FXML private Button btnConfirmDeliveryPayment;

    private order currentOrder;
    private double totalAmount;
    private OrderService orderService;
    private StripePaymentService stripeService;

    private boolean paymentInProgress = false;
    private boolean paymentCompleted = false;
    private String sessionId = null;

    @FXML
    public void initialize() {
        // Initialize services with error handling
        try {
            orderService = new OrderService();
        } catch (Exception e) {
            orderService = null;
            showError("Initialization Error", "Failed to initialize OrderService: " + e.getMessage());
            return;
        }
        try {
            stripeService = new StripePaymentService();
        } catch (Exception e) {
            stripeService = null;
            showError("Initialization Error", "Failed to initialize StripePaymentService: " + e.getMessage());
            return;
        }

        // Set up payment method toggle handlers
        rbCreditCard.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                creditCardForm.setVisible(true);
                payAtDeliveryForm.setVisible(false);
            }
        });

        rbPayAtDelivery.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                creditCardForm.setVisible(false);
                payAtDeliveryForm.setVisible(true);
            }
        });
    }

    public void setOrderData(order order, double amount) {
        this.currentOrder = order;
        this.totalAmount = amount;

        if (orderService == null || stripeService == null) {
            showError("Service Error", "Payment services are not available.");
            return;
        }

        lblOrderId.setText("Order #" + order.getId());
        lblAmount.setText(String.format("Total Amount: %.2f TND", amount));
    }

    @FXML
    private void openStripeCheckout() {
        if (paymentInProgress || stripeService == null) return;

        paymentInProgress = true;
        btnPayWithStripe.setDisable(true);
        btnPayWithStripe.setText("Processing...");
        loadingIndicator.setVisible(true);

        String baseUrl = "https://agrosphere.app/payment";
        String successUrl = baseUrl + "/success?session_id={CHECKOUT_SESSION_ID}&order_id=" + currentOrder.getId();
        String cancelUrl = baseUrl + "/cancel?session_id={CHECKOUT_SESSION_ID}&order_id=" + currentOrder.getId();

        CompletableFuture.supplyAsync(() -> {
            try {
                return stripeService.createCheckoutSession(currentOrder, totalAmount, successUrl, cancelUrl);
            } catch (StripeException e) {
                throw new RuntimeException("Stripe session creation failed", e);
            }
        }).thenAccept(checkoutUrl -> {
            Platform.runLater(() -> {
                try {
                    webViewStripe.setVisible(true);
                    WebEngine engine = webViewStripe.getEngine();
                    engine.load(null);
                    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            handlePageLoad(engine.getLocation());
                        }
                    });
                    engine.load(checkoutUrl);
                    loadingIndicator.setVisible(false);
                } catch (Exception e) {
                    handlePaymentError("Failed to load checkout", e);
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> handlePaymentError("Payment processing failed", e));
            return null;
        });
    }

    private void handlePageLoad(String url) {
        System.out.println("Navigated to: " + url);
        if (url == null) return;

        if (url.contains("/success")) {
            sessionId = extractSessionId(url);
            handleSuccessfulPayment();
        } else if (url.contains("/cancel")) {
            handleCancelledPayment();
        }
    }

    private String extractSessionId(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("session_id")) {
                        return keyValue[1];
                    }
                }
            }
            Pattern pattern = Pattern.compile("session_id=([^&]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (URISyntaxException e) {
            System.err.println("Error parsing URL: " + e.getMessage());
        }
        return null;
    }

    private void handleSuccessfulPayment() {
        if (paymentCompleted || orderService == null) return;
        paymentCompleted = true;

        try {
            if (sessionId != null && stripeService.verifyPayment(sessionId)) {
                currentOrder.setStatus("paid");
                orderService.update(currentOrder);
                showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Your payment has been processed successfully. Thank you for your order!");
                Platform.runLater(this::closeWindow);
            } else {
                throw new Exception("Payment verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Payment Verification Failed", e.getMessage());
        }
    }

    private void handleCancelledPayment() {
        paymentInProgress = false;
        btnPayWithStripe.setDisable(false);
        btnPayWithStripe.setText("Pay with Stripe");
        webViewStripe.setVisible(false);
        showAlert(Alert.AlertType.INFORMATION, "Payment Cancelled", "You've cancelled the payment process. Your order is still pending.");
    }

    private void handlePaymentError(String message, Throwable e) {
        paymentInProgress = false;
        btnPayWithStripe.setDisable(false);
        btnPayWithStripe.setText("Pay with Stripe");
        loadingIndicator.setVisible(false);
        webViewStripe.setVisible(false);
        String errorMsg = message + ": " + e.getMessage();
        System.err.println(errorMsg);
        showError("Payment Error", errorMsg);
    }

    @FXML
    private void confirmPayAtDelivery() {
        if (orderService == null) return;
        try {
            currentOrder.setStatus("awaiting_delivery");
            orderService.update(currentOrder);
            showAlert(Alert.AlertType.INFORMATION, "Order Confirmed", "Your order has been confirmed. Payment will be collected on delivery.");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Order Confirmation Failed", e.getMessage());
        }
    }

    @FXML
    private void cancelPayment() {
        if (paymentInProgress && !paymentCompleted) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancel Payment");
            alert.setHeaderText("Are you sure you want to cancel?");
            alert.setContentText("This will cancel your current payment process.");
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                closeWindow();
            }
        } else {
            closeWindow();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) lblOrderId.getScene().getWindow();
        stage.close();
    }
}