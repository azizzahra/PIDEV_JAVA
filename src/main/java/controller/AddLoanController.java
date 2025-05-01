package controller;

import com.stripe.exception.StripeException;
import entities.Loan;
import entities.Place;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import services.LoanService;
import services.PaymentService;

import java.sql.SQLException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AddLoanController {
    @FXML private TextField priceField;
    @FXML private TextField ticketsLeftField;
    @FXML private TextField formationField;
    @FXML private TextField imageField;
    @FXML private ComboBox<Place> placeCombo;

    private LoanService loanService;
    private Runnable refreshCallback;
    private ObservableList<Place> placeData;

    @FXML
    public void initialize() {
        // Configure numeric input validation
        configureNumericField(priceField, true);  // Allow decimals for price
        configureNumericField(ticketsLeftField, false); // Integers only for tickets

        // Configure image URL validation
        imageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !isValidImageUrl(newVal)) {
                imageField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
            } else {
                imageField.setStyle("");
            }
        });
    }

    public void setLoanService(LoanService loanService) {
        this.loanService = loanService;
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void setPlaceData(ObservableList<Place> places) {
        this.placeData = places;
        placeCombo.setItems(placeData);

        placeCombo.setConverter(new StringConverter<Place>() {
            @Override
            public String toString(Place place) {
                return place != null ? place.getName() + " (Capacity: " + place.getCapacity() + ")" : "";
            }

            @Override
            public Place fromString(String string) {
                return placeData.stream()
                        .filter(p -> p.getName().equals(string.split(" ")[0]))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Style the ComboBox
        placeCombo.setStyle("-fx-background-color: #ffffff; -fx-border-color: #a3b18a;");
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            Loan newLoan = new Loan(
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(ticketsLeftField.getText()),
                    formationField.getText().trim(),
                    imageField.getText().trim(),
                    placeCombo.getValue()
            );

            // Create a Stripe Checkout Session
            PaymentService paymentService = new PaymentService();
            String checkoutUrl = paymentService.createCheckoutSession(newLoan);

            // Open the Stripe Checkout page in the system browser
            java.awt.Desktop.getDesktop().browse(new java.net.URI(checkoutUrl));

            // Optionally: Only add to DB after payment success (via webhook)
            // loanService.ajouter(newLoan);
            // refreshCallback.run();
            // closeWindow();

        } catch (StripeException e) {
            showAlert("Stripe Error", "Payment failed: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (placeCombo.getValue() == null) {
            errors.append("- Please select a place\n");
            placeCombo.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            placeCombo.setStyle("-fx-border-color: #a3b18a;");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required\n");
            priceField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            priceField.setStyle("");
        }

        if (ticketsLeftField.getText().trim().isEmpty()) {
            errors.append("- Tickets available is required\n");
            ticketsLeftField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            ticketsLeftField.setStyle("");
        }

        if (formationField.getText().trim().isEmpty()) {
            errors.append("- Formation is required\n");
            formationField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            formationField.setStyle("");
        }

        if (!imageField.getText().isEmpty() && !isValidImageUrl(imageField.getText())) {
            errors.append("- Invalid image URL format\n");
            imageField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1px;");
        } else {
            imageField.setStyle("");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void configureNumericField(TextField field, boolean allowDecimal) {
        Pattern pattern = allowDecimal ? Pattern.compile("\\d*\\.?\\d*") : Pattern.compile("\\d*");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private boolean isValidImageUrl(String url) {
        return url.matches("^(https?|ftp)://.*\\.(jpg|jpeg|png|gif|bmp)$") ||
                url.matches("^.*\\.(jpg|jpeg|png|gif|bmp)$");
    }

    private void closeWindow() {
        priceField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        alert.getDialogPane().setStyle(
                "-fx-background-color: #f5f5f0; " +
                        "-fx-border-color: #3a5a40; " +
                        "-fx-border-width: 2px;"
        );

        alert.showAndWait();
    }
}